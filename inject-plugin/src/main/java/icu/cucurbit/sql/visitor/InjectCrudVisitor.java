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

import java.util.List;
import java.util.Optional;

public class InjectCrudVisitor extends StatementVisitorAdapter {

    private final JdbcIndexAndParameters parameterAdder;
    public InjectCrudVisitor(JdbcIndexAndParameters parameterAdder) {
        this.parameterAdder = parameterAdder;
    }

    @Override
    public void visit(Delete delete) {
        Table table = delete.getTable();
        if (table != null) {
            Expression newExpression = visitFromItem(table, delete.getWhere());
            delete.setWhere(newExpression);
        }

        List<Join> joins = delete.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                Expression newExpression = visitFromItem(join.getRightItem(), delete.getWhere());
                delete.setWhere(newExpression);
            }
        }

        Expression where = delete.getWhere();
        if (where != null) {
            where.accept(new InjectExpressionVisitor(this.parameterAdder));
        }
    }

    @Override
    public void visit(Update update) {
        // tables.
        List<Table> tables = update.getTables();
        if (tables != null) {
            for (Table table : tables) {
                Expression newExpression = visitFromItem(table, update.getWhere());
                update.setWhere(newExpression);
            }
        }

        // from.
        FromItem fromItem = update.getFromItem();
        if (fromItem != null) {
            Expression newExpression = visitFromItem(fromItem, update.getWhere());
            update.setWhere(newExpression);
        }

        // join.
        List<Join> joins = update.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                Expression newExpression = visitFromItem(join.getRightItem(), update.getWhere());
                update.setWhere(newExpression);
            }
        }

        // where.
        Expression where = update.getWhere();
        if (where != null) {
            where.accept(new InjectExpressionVisitor(this.parameterAdder));
        }

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


    /**
     * where 添加 and 条件.
     */
    private Expression injectWhereCondition(Expression originCondition, Table table) {
        String tableName = table.getName();
        String aliasName = Optional.ofNullable(table.getAlias()).map(Alias::getName).orElse(tableName);

        List<TableRule> rules = RuleContext.getRules();

        Expression expression = null;
        for (TableRule tableRule : rules) {
            String matchTable = tableRule.getTableName();
            if (tableName.equalsIgnoreCase(matchTable)) {
                tableRule.setTableName(aliasName);
                String expressionStr = tableRule.toExpressionString();
                Expression attachExpression;
                try {
                    attachExpression = CCJSqlParserUtil.parseCondExpression(expressionStr);
                } catch (JSQLParserException e) {
                    throw new IllegalArgumentException("illegal cond expression: " + expressionStr);
                }
                expression = expression == null ? attachExpression : new AndExpression(expression, attachExpression);
                this.parameterAdder.addParameter(tableRule.getTarget());
            }
        }
        if (expression != null) {
            originCondition = originCondition == null ? expression : new AndExpression(expression, originCondition);
        }
        return originCondition;
    }

    private Expression visitFromItem(FromItem fromItem, Expression oldExpression) {
        InjectFromItemVisitor fromItemVisitor = new InjectFromItemVisitor(this.parameterAdder);
        fromItem.accept(fromItemVisitor);
        if(fromItemVisitor.foundTable()) {
            oldExpression = injectWhereCondition(oldExpression, fromItemVisitor.getTable());
        }
        return oldExpression;
    }


}
