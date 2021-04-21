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
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InjectSelectVisitor implements SelectVisitor {

    private JdbcIndexAndParameters parameterAdder;

    public InjectSelectVisitor(JdbcIndexAndParameters parameterAdder) {
        this.parameterAdder = parameterAdder;
    }

    @Override
    public void visit(PlainSelect plainSelect) {

        // from.
        FromItem fromItem = plainSelect.getFromItem();
        if (Objects.nonNull(fromItem)) {
            InjectFromItemVisitor fromItemVisitor = new InjectFromItemVisitor(this.parameterAdder);
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
                InjectFromItemVisitor joinItemVisitor = new InjectFromItemVisitor(this.parameterAdder);
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
			where.accept(new InjectExpressionVisitor(this.parameterAdder));
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

                if (tableRule.getTarget() instanceof Collection) {
                    this.parameterAdder.addParameters((Collection) tableRule.getTarget());
                } else {
                    this.parameterAdder.addParameter(tableRule.getTarget());
                }
            }
        }
        if (expression != null) {
            Expression whereExpression = plainSelect.getWhere();
            whereExpression = whereExpression == null ? expression : new AndExpression(expression, whereExpression);
            plainSelect.setWhere(whereExpression);
        }
    }
}
