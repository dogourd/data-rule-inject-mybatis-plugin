package icu.cucurbit.sql.visitor;

import icu.cucurbit.RuleContext;
import icu.cucurbit.sql.JdbcIndexAndParameters;
import icu.cucurbit.sql.TableRule;
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

import java.util.*;

public class InjectCrudVisitor extends StatementVisitorAdapter {

    private final JdbcIndexAndParameters parameterAdder;

    public InjectCrudVisitor(JdbcIndexAndParameters parameterAdder) {
        this.parameterAdder = parameterAdder;
    }

    @Override
    public void visit(Delete delete) {
        List<TableRule> suitableRules = new ArrayList<>();

        Table table = delete.getTable();
        Optional.ofNullable(table).ifPresent(t -> {
            List<TableRule> rules = visitFromItem(table);
            suitableRules.addAll(rules);
        });

        List<Join> joins = delete.getJoins();
        Optional.ofNullable(joins).orElse(Collections.emptyList()).forEach(item -> {
            List<TableRule> rules = visitFromItem(item.getRightItem());
            suitableRules.addAll(rules);
        });

        Expression where = delete.getWhere();
        if (where != null) {
            where.accept(new InjectExpressionVisitor(this.parameterAdder));
        }

        if (suitableRules.isEmpty()) {
            return;
        }

        Expression newWhere = assembleExpression(where, suitableRules);
        delete.setWhere(newWhere);
    }


    @Override
    public void visit(Update update) {
        List<TableRule> suitableRules = new ArrayList<>();
        // tables.
        List<Table> tables = update.getTables();
        Optional.ofNullable(tables).orElse(Collections.emptyList()).forEach(table -> {
            List<TableRule> rules = visitFromItem(table);
            suitableRules.addAll(rules);
        });

        // from.
        FromItem fromItem = update.getFromItem();
        Optional.ofNullable(fromItem).ifPresent(fi -> {
            List<TableRule> rules = visitFromItem(fi);
            suitableRules.addAll(rules);
        });

        // set.
        InjectExpressionVisitor setVisitor = new InjectExpressionVisitor(parameterAdder);
        List<Expression> expressions = update.getExpressions();
        Optional.ofNullable(expressions).orElse(Collections.emptyList())
                .forEach(expression -> expression.accept(setVisitor));

        // join.
        List<Join> joins = update.getJoins();
        Optional.ofNullable(joins).orElse(Collections.emptyList()).forEach(join -> {
            List<TableRule> rules = visitFromItem(join.getRightItem());
            suitableRules.addAll(rules);
        });

        // where.
        Expression where = update.getWhere();
        if (where != null) {
            where.accept(new InjectExpressionVisitor(this.parameterAdder));
        }

        Expression newWhere = assembleExpression(where, suitableRules);
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

    private List<TableRule> getSuitableRules(Table table) {
        String tableName = table.getName();
        String aliasName = Optional.ofNullable(table.getAlias()).map(Alias::getName).orElse(tableName);

        List<TableRule> rules = RuleContext.getRules();
        List<TableRule> result = new ArrayList<>();
        for (TableRule rule : rules) {
            if (tableName.equals(rule.getTableName())) {
                TableRule copyRule = new TableRule();
                copyRule.setTableName(aliasName);
                copyRule.setRelation(rule.getRelation());
                copyRule.setProperty(rule.getProperty());
                copyRule.setTarget(rule.getTarget());
                result.add(copyRule);
            }
        }
        return result;
    }

    private Expression assembleExpression(Expression oldExpression, List<TableRule> rules) {
        Expression finalExpression = null;
        for (TableRule rule : rules) {
            String expressionStr = rule.toExpressionString();
            try {
                Expression expression = CCJSqlParserUtil.parseCondExpression(expressionStr);
                finalExpression = finalExpression == null ? expression : new AndExpression(finalExpression, expression);
                parameterAdder.addParameter(rule.getTarget());
            } catch (JSQLParserException ignore) {
            }
        }
        if (finalExpression == null) {
            return oldExpression;
        }

        return oldExpression == null ? finalExpression : new AndExpression(oldExpression, finalExpression);

    }




    private List<TableRule> visitFromItem(FromItem fromItem) {
        InjectFromItemVisitor fromItemVisitor = new InjectFromItemVisitor(parameterAdder);
        fromItem.accept(fromItemVisitor);
        if (fromItemVisitor.foundTable()) {
            return getSuitableRules(fromItemVisitor.getTable());
        }
        return Collections.emptyList();
    }



}
