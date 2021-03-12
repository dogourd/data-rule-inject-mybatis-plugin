package icu.cucurbit.sql.visitor;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

public class InjectExpressionVisitor extends ExpressionVisitorAdapter {

    @Override
    public void visit(SignedExpression signedExpression) {
        signedExpression.accept(InjectVisitors.EXPRESSION_VISITOR);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(InjectVisitors.EXPRESSION_VISITOR);
    }

    // between
    @Override
    public void visit(Between between) {
        between.getLeftExpression().accept(InjectVisitors.EXPRESSION_VISITOR);
        between.getBetweenExpressionStart().accept(InjectVisitors.EXPRESSION_VISITOR);
        between.getBetweenExpressionEnd().accept(InjectVisitors.EXPRESSION_VISITOR);
    }

    // in表达式
    @Override
    public void visit(InExpression inExpression) {
        if (inExpression.getLeftExpression() != null) {
            inExpression.getLeftExpression().accept(InjectVisitors.EXPRESSION_VISITOR);
        } else if (inExpression.getLeftItemsList() != null) {
            ItemsList leftItemsList = inExpression.getLeftItemsList();
            leftItemsList.accept(InjectVisitors.ITEMS_LIST_VISITOR);
        }
        inExpression.getRightItemsList().accept(InjectVisitors.ITEMS_LIST_VISITOR);
    }

    // 子查询
    @Override
    public void visit(SubSelect subSelect) {
        if (subSelect.getWithItemsList() != null) {
            for (WithItem withItem : subSelect.getWithItemsList()) {
                withItem.accept(InjectVisitors.SELECT_VISITOR);
            }
        }
        subSelect.getSelectBody().accept(InjectVisitors.SELECT_VISITOR);
    }

    // exist
    @Override
    public void visit(ExistsExpression existsExpression) {
        existsExpression.getRightExpression().accept(InjectVisitors.EXPRESSION_VISITOR);
    }

    // allComparisonExpression??
    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().getSelectBody()
                .accept(InjectVisitors.SELECT_VISITOR);
    }

    // anyComparisonExpression??
    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().getSelectBody()
                .accept(InjectVisitors.SELECT_VISITOR);
    }



    // rowConstructor?
    @Override
    public void visit(RowConstructor rowConstructor) {
        for (Expression expr : rowConstructor.getExprList().getExpressions()) {
            expr.accept(InjectVisitors.EXPRESSION_VISITOR);
        }
    }

    // cast
    @Override
    public void visit(CastExpression cast) {
        cast.getLeftExpression().accept(InjectVisitors.EXPRESSION_VISITOR);
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
        binaryExpression.getLeftExpression().accept(InjectVisitors.EXPRESSION_VISITOR);
        binaryExpression.getRightExpression().accept(InjectVisitors.EXPRESSION_VISITOR);
    }


}
