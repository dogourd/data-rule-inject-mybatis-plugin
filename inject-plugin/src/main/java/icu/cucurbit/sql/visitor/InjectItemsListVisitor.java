package icu.cucurbit.sql.visitor;

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitorAdapter;
import net.sf.jsqlparser.statement.select.SubSelect;

public class InjectItemsListVisitor extends ItemsListVisitorAdapter {

    @Override
    public void visit(SubSelect ss) {
        ss.getSelectBody().accept(InjectVisitors.SELECT_VISITOR);
    }

    @Override
    public void visit(ExpressionList el) {
        List<Expression> list = el.getExpressions();
        if (list != null && list.size() > 0) {
            for (Expression expr : list) {
                expr.accept(InjectVisitors.EXPRESSION_VISITOR);
            }
        }
    }



}  