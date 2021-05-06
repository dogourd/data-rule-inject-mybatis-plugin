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

import java.util.*;

public class InjectSelectVisitor implements SelectVisitor {

    private JdbcIndexAndParameters parameterAdder;

    public InjectSelectVisitor(JdbcIndexAndParameters parameterAdder) {
        this.parameterAdder = parameterAdder;
    }

    @Override
    public void visit(PlainSelect plainSelect) {

        List<TableRule> suitableRules = new ArrayList<>();
        // from.
        FromItem fromItem = plainSelect.getFromItem();
        Optional.ofNullable(fromItem).ifPresent(fi -> {
            List<TableRule> rules = visitFromItem(fi);
            suitableRules.addAll(rules);
        });

        // join.
        List<Join> joins = plainSelect.getJoins();
        Optional.ofNullable(joins).orElse(Collections.emptyList()).forEach(j -> {
            List<TableRule> rules = visitFromItem(j.getRightItem());
            suitableRules.addAll(rules);
        });

		// where
		Expression where = plainSelect.getWhere();
		if (where != null) {
			where.accept(new InjectExpressionVisitor(this.parameterAdder));
		}

        Expression newWhere = assembleExpression(where, suitableRules);
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

    private List<TableRule> visitFromItem(FromItem fromItem) {
        InjectFromItemVisitor fromItemVisitor = new InjectFromItemVisitor(parameterAdder);
        fromItem.accept(fromItemVisitor);
        if (fromItemVisitor.foundTable()) {
            return getSuitableRules(fromItemVisitor.getTable());
        }
        return Collections.emptyList();
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



}
