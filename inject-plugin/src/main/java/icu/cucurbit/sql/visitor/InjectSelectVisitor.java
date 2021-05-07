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
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class InjectSelectVisitor implements SelectVisitor {

    private final JdbcIndexAndParameters parameterAdder;

    public InjectSelectVisitor(JdbcIndexAndParameters parameterAdder) {
        this.parameterAdder = parameterAdder;
    }

    @Override
    public void visit(PlainSelect plainSelect) {

        List<Pair<FilterTable, RuleFilter>> pairs = new ArrayList<>();
        // from.
        FromItem fromItem = plainSelect.getFromItem();
        Optional.ofNullable(fromItem).ifPresent(fi -> {
            List<Pair<FilterTable, RuleFilter>> rules = visitFromItem(fi);
            pairs.addAll(rules);
        });

        // join.
        List<Join> joins = plainSelect.getJoins();
        Optional.ofNullable(joins).orElse(Collections.emptyList()).forEach(j -> {
            List<Pair<FilterTable, RuleFilter>> rules = visitFromItem(j.getRightItem());
            pairs.addAll(rules);
        });

        // where
        Expression where = plainSelect.getWhere();
        if (where != null) {
            where.accept(new InjectExpressionVisitor(this.parameterAdder));
        }

        Expression newWhere = assembleExpression(where, pairs);
        plainSelect.setWhere(newWhere);
    }

    @Override
    public void visit(SetOperationList setOpList) {
        List<SelectBody> selects = setOpList.getSelects();
        for (SelectBody selectBody : selects) {
            selectBody.accept(this);
        }
    }

    @Override
    public void visit(WithItem withItem) {
        SelectBody selectBody = withItem.getSelectBody();
        selectBody.accept(this);
    }

    @Override
    public void visit(ValuesStatement aThis) {
        throw new UnsupportedOperationException("inject table rule cannot visit values statement");
    }


    private List<RuleFilter> getSuitableRules(Table table) {
        String tableName = table.getName();

        List<RuleFilter> rules = FilterContext.getFilters();
        List<RuleFilter> result = new ArrayList<>();
        for (RuleFilter rule : rules) {
            if (tableName.equals(rule.getTable().getName())) {
                result.add(rule);
            }
        }
        return result;
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
        if (oldExpression == null) {
            return finalExpression;
        }
        if (oldExpression instanceof OrExpression) {
            return new AndExpression(new Parenthesis(oldExpression), finalExpression);
        }
        return new AndExpression(oldExpression, finalExpression);

    }


}
