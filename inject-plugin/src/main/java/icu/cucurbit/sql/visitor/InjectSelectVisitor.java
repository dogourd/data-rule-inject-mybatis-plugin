package icu.cucurbit.sql.visitor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import icu.cucurbit.RuleContext;
import icu.cucurbit.sql.TableRule;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class InjectSelectVisitor implements SelectVisitor {

    public InjectSelectVisitor() {
    }

    @Override
    public void visit(PlainSelect plainSelect) {

        // from.
        FromItem fromItem = plainSelect.getFromItem();
        if (Objects.nonNull(fromItem)) {
            InjectFromItemVisitor fromItemVisitor = new InjectFromItemVisitor();
            fromItem.accept(fromItemVisitor);

            // from table.
            if (fromItemVisitor.foundTable()) {
                injectWhereCondition(plainSelect, fromItemVisitor.getTable());
            }
        }

        // join.
        List<Join> joins = plainSelect.getJoins();
        if (Objects.nonNull(joins)) {
            for (Join join : joins) {
                FromItem joinItem = join.getRightItem();
                InjectFromItemVisitor joinItemVisitor = new InjectFromItemVisitor();
                joinItem.accept(joinItemVisitor);

                // join table.
                if (joinItemVisitor.foundTable()) {
                    injectWhereCondition(plainSelect, joinItemVisitor.getTable());
                }
            }
        }

		// where
		Expression where = plainSelect.getWhere();
		if (where != null) {
			where.accept(InjectVisitors.EXPRESSION_VISITOR);
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


    /**
	 * where 添加 and 条件.
	 * */
    private void injectWhereCondition(PlainSelect plainSelect, Table table) {
        String tableName = table.getName();
        String aliasName = Optional.ofNullable(table.getAlias()).map(Alias::getName).orElse(tableName);

        List<TableRule> rules = RuleContext.getRules();

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
                Expression originCondition = plainSelect.getWhere();

                Expression newCondition = originCondition == null
                        ? attachExpression : new AndExpression(originCondition, attachExpression);
                plainSelect.setWhere(newCondition);
            }
        }
    }
}
