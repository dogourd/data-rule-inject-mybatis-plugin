package icu.cucurbit.sql.visitor;

import icu.cucurbit.sql.JdbcIndexAndParameters;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * if select.
 *  get where.
 * */
public class InjectExpressionVisitor extends ExpressionVisitorAdapter {

    // getIndex return 1; need 0 in list. return 0 next is 1;;
    private JdbcIndexAndParameters parameterAdder;

    public InjectExpressionVisitor(JdbcIndexAndParameters parameterAdder) {
        this.parameterAdder = parameterAdder;
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        signedExpression.accept(this);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    // between
    @Override
    public void visit(Between between) {
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    // in表达式
    @Override
    public void visit(InExpression inExpression) {
        InjectItemsListVisitor itemsListVisitor = new InjectItemsListVisitor(this.parameterAdder);
        if (inExpression.getLeftExpression() != null) {
            inExpression.getLeftExpression().accept(this);
        } else if (inExpression.getLeftItemsList() != null) {
            ItemsList leftItemsList = inExpression.getLeftItemsList();
            leftItemsList.accept(itemsListVisitor);
        }
        inExpression.getRightItemsList().accept(itemsListVisitor);
    }

    // 子查询
    @Override
    public void visit(SubSelect subSelect) {
        InjectSelectVisitor selectVisitor = new InjectSelectVisitor(this.parameterAdder);
        if (subSelect.getWithItemsList() != null) {
            for (WithItem withItem : subSelect.getWithItemsList()) {
                withItem.accept(selectVisitor);
            }
        }
        subSelect.getSelectBody().accept(selectVisitor);
    }

    // exist
    @Override
    public void visit(ExistsExpression existsExpression) {
        existsExpression.getRightExpression().accept(this);
    }

    // allComparisonExpression??
    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().getSelectBody()
                .accept(new InjectSelectVisitor(this.parameterAdder));
    }

    // anyComparisonExpression??
    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().getSelectBody()
                .accept(new InjectSelectVisitor(this.parameterAdder));
    }



    // rowConstructor?
    @Override
    public void visit(RowConstructor rowConstructor) {
        for (Expression expr : rowConstructor.getExprList().getExpressions()) {
            expr.accept(this);
        }
    }

    // cast
    @Override
    public void visit(CastExpression cast) {
        cast.getLeftExpression().accept(this);
    }

    // 加法
    @Override
    public void visit(Addition addition) {
        visitBinaryExpression(addition);
    }

    // 除法
    @Override
    public void visit(Division division) {
        visitBinaryExpression(division);
    }

    // 乘法
    @Override
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication);
    }

    // 减法
    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction);
    }

    // and表达式
    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    // or表达式
    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression);
    }

    // 等式
    @Override
    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);
    }

    // 大于
    @Override
    public void visit(GreaterThan greaterThan) {
        visitBinaryExpression(greaterThan);
    }

    // 大于等于
    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        visitBinaryExpression(greaterThanEquals);
    }

    // like表达式
    @Override
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression);
    }

    // 小于
    @Override
    public void visit(MinorThan minorThan) {
        visitBinaryExpression(minorThan);
    }

    // 小于等于
    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        visitBinaryExpression(minorThanEquals);
    }

    // 不等于
    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
    }

    // concat
    @Override
    public void visit(Concat concat) {
        visitBinaryExpression(concat);
    }

    // matches?
    @Override
    public void visit(Matches matches) {
        visitBinaryExpression(matches);
    }

    // bitwiseAnd位运算?
    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        visitBinaryExpression(bitwiseAnd);
    }

    // bitwiseOr?
    @Override
    public void visit(BitwiseOr bitwiseOr) {
        visitBinaryExpression(bitwiseOr);
    }

    // bitwiseXor?
    @Override
    public void visit(BitwiseXor bitwiseXor) {
        visitBinaryExpression(bitwiseXor);
    }

    // 取模运算modulo?
    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo);
    }

    // rexp??
    @Override
    public void visit(RegExpMatchOperator rexpr) {
        visitBinaryExpression(rexpr);
    }

    // regExpMySQLOperator??
    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        visitBinaryExpression(regExpMySQLOperator);
    }

    // 二元表达式
    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(JdbcParameter parameter) {
        this.parameterAdder.skipOne();
    }
}
