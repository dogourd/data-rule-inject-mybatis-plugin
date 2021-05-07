package icu.cucurbit.sql.visitor;

import icu.cucurbit.FilterContext;
import icu.cucurbit.bo.FilterTable;
import icu.cucurbit.bo.Pair;
import icu.cucurbit.sql.JdbcIndexAndParameters;
import icu.cucurbit.sql.filter.RuleFilter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class InjectCrudVisitor extends StatementVisitorAdapter {

    private final JdbcIndexAndParameters parameterAdder;

    public InjectCrudVisitor(JdbcIndexAndParameters parameterAdder) {
        this.parameterAdder = parameterAdder;
    }

    @Override
    public void visit(Delete delete) {
        List<Pair<FilterTable, RuleFilter>> pairs = new ArrayList<>();

        Table table = delete.getTable();
        Optional.ofNullable(table).ifPresent(t -> {
            List<Pair<FilterTable, RuleFilter>> rules = visitFromItem(table);
            pairs.addAll(rules);
        });

        List<Join> joins = delete.getJoins();
        Optional.ofNullable(joins).orElse(Collections.emptyList()).forEach(item -> {
            List<Pair<FilterTable, RuleFilter>> rules = visitFromItem(item.getRightItem());
            pairs.addAll(rules);
        });

        Expression where = delete.getWhere();
        if (where != null) {
            where.accept(new InjectExpressionVisitor(this.parameterAdder));
        }

        if (pairs.isEmpty()) {
            return;
        }

        Expression newWhere = assembleExpression(where, pairs);
        delete.setWhere(newWhere);
    }


    @Override
    public void visit(Update update) {
        List<Pair<FilterTable, RuleFilter>> pairs = new ArrayList<>();
        // tables.
        List<Table> tables = update.getTables();
        Optional.ofNullable(tables).orElse(Collections.emptyList()).forEach(table -> {
            List<Pair<FilterTable, RuleFilter>> rules = visitFromItem(table);
            pairs.addAll(rules);
        });

        // from.
        FromItem fromItem = update.getFromItem();
        Optional.ofNullable(fromItem).ifPresent(fi -> {
            List<Pair<FilterTable, RuleFilter>> rules = visitFromItem(fi);
            pairs.addAll(rules);
        });

        // set.
        InjectExpressionVisitor setVisitor = new InjectExpressionVisitor(parameterAdder);
        List<Expression> expressions = update.getExpressions();
        Optional.ofNullable(expressions).orElse(Collections.emptyList())
                .forEach(expression -> expression.accept(setVisitor));

        // join.
        List<Join> joins = update.getJoins();
        Optional.ofNullable(joins).orElse(Collections.emptyList()).forEach(join -> {
            List<Pair<FilterTable, RuleFilter>> rules = visitFromItem(join.getRightItem());
            pairs.addAll(rules);
        });

        // where.
        Expression where = update.getWhere();
        if (where != null) {
            where.accept(new InjectExpressionVisitor(this.parameterAdder));
        }

        Expression newWhere = assembleExpression(where, pairs);
        update.setWhere(newWhere);
    }

    @Override
    public void visit(Insert insert) {

    }

    @Override
    public void visit(Select select) {
        List<WithItem> withItems = select.getWithItemsList();
        InjectSelectVisitor selectVisitor = new InjectSelectVisitor(this.parameterAdder);
        if (withItems != null) {
            withItems.forEach(selectVisitor::visit);
        }

        SelectBody selectBody = select.getSelectBody();
        if (selectBody != null) {
            selectBody.accept(selectVisitor);
        }
    }

    private List<RuleFilter> getSuitableRules(Table table) {
        String tableName = table.getName();

        List<RuleFilter> filters = FilterContext.getFilters();
        List<RuleFilter> result = new ArrayList<>();
        for (RuleFilter rule : filters) {
            if (tableName.equals(rule.getTable().getName())) {
                result.add(rule);
            }
        }
        return result;
    }

    private Expression assembleExpression(Expression oldExpression, List<Pair<FilterTable, RuleFilter>> pairs) {
        Expression finalExpression = null;
        Supplier<String> placeHolderSupplier = () -> "?";
        for (Pair<FilterTable, RuleFilter> pair : pairs) {
            FilterTable table = pair.getLeft();
            RuleFilter filter = pair.getRight();

            String expressionStr = filter.toSqlSnippet(table, placeHolderSupplier);
            try {
                Expression expression = CCJSqlParserUtil.parseCondExpression(expressionStr);
                finalExpression = finalExpression == null ? expression : new AndExpression(finalExpression, expression);
                parameterAdder.addParameter(filter.getJdbcParameters());
            } catch (JSQLParserException ex) {
                log.warn("cannot parse cond expression {}", expressionStr);
            }
        }
        if (finalExpression == null) {
            return oldExpression;
        }

        return oldExpression == null ? finalExpression : new AndExpression(oldExpression, finalExpression);
    }



    private List<Pair<FilterTable, RuleFilter>> visitFromItem(FromItem fromItem) {
        InjectFromItemVisitor fromItemVisitor = new InjectFromItemVisitor(parameterAdder);
        fromItem.accept(fromItemVisitor);
        if (fromItemVisitor.foundTable()) {
            List<RuleFilter> filters = getSuitableRules(fromItemVisitor.getTable());
            Table table = fromItemVisitor.getTable();
            FilterTable filterTable = new FilterTable(table.getName(),
                    Optional.ofNullable(table.getAlias()).map(Alias::getName).orElse(table.getName()));
            return filters.stream().map(filter -> new Pair<>(filterTable, filter)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }



}
