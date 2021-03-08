package icu.cucurbit.sql.visitor;

import icu.cucurbit.sql.TableRule;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InjectTableRuleSelectVisitor implements SelectVisitor {

    private final List<TableRule> rules;

    public InjectTableRuleSelectVisitor(List<TableRule> rules) {
        this.rules = rules;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        // from.
        FromItem fromItem = plainSelect.getFromItem();
        if (Objects.isNull(fromItem)) {
            return;
        }
        InjectTableRuleFromItemVisitor fromItemVisitor = new InjectTableRuleFromItemVisitor(this);
        fromItem.accept(fromItemVisitor);

        if (fromItemVisitor.foundTable()) {
            injectWhereCondition(plainSelect, fromItemVisitor.getTable());
        }

        // join.
        List<Join> joins = plainSelect.getJoins();
        if (Objects.isNull(joins)) {
            return;
        }
        for (Join join : joins) {
            FromItem joinItem = join.getRightItem();
            InjectTableRuleFromItemVisitor joinItemVisitor = new InjectTableRuleFromItemVisitor(this);
            joinItem.accept(joinItemVisitor);

            if (joinItemVisitor.foundTable()) {
                injectWhereCondition(plainSelect, joinItemVisitor.getTable());
            }
        }
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


    private void injectWhereCondition(PlainSelect plainSelect, Table table) {
        String tableName = table.getName();
        String aliasName = Optional.ofNullable(table.getAlias()).map(Alias::getName).orElse(null);

        for (TableRule tableRule : rules) {
            Expression originCondition = plainSelect.getWhere();
            String matchTable = tableRule.getTableName();
            if (tableName.equalsIgnoreCase(matchTable)) {
                tableRule.setTableName(Objects.toString(aliasName, tableName));
                String expressionStr = tableRule.toExpressionString();
                Expression attachExpression;
                try {
                    attachExpression = CCJSqlParserUtil.parseCondExpression(expressionStr);
                } catch (JSQLParserException e) {
                    throw new IllegalArgumentException("illegal cond expression: " + expressionStr);
                }
                Expression newCondition = originCondition == null
                        ? attachExpression : new AndExpression(originCondition, attachExpression);
                plainSelect.setWhere(newCondition);
            }
        }
    }
}
