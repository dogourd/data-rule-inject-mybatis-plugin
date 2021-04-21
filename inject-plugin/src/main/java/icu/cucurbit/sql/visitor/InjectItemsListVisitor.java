package icu.cucurbit.sql.visitor;

import icu.cucurbit.sql.JdbcIndexAndParameters;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitorAdapter;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.List;

public class InjectItemsListVisitor extends ItemsListVisitorAdapter {

    private JdbcIndexAndParameters parameterAdder;
    public InjectItemsListVisitor(JdbcIndexAndParameters parameterAdder) {
        this.parameterAdder = parameterAdder;
    }
    @Override
    public void visit(SubSelect ss) {
        ss.getSelectBody().accept(new InjectSelectVisitor(this.parameterAdder));
    }

    @Override
    public void visit(ExpressionList el) {
        InjectExpressionVisitor expressionVisitor = new InjectExpressionVisitor(this.parameterAdder);
        List<Expression> list = el.getExpressions();
        if (list != null && list.size() > 0) {
            for (Expression expr : list) {
                expr.accept(expressionVisitor);
            }
        }
    }



}  