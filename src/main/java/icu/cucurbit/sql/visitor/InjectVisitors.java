package icu.cucurbit.sql.visitor;

public class InjectVisitors {

	public static InjectSelectVisitor SELECT_VISITOR = new InjectSelectVisitor();
	public static InjectItemsListVisitor ITEMS_LIST_VISITOR = new InjectItemsListVisitor();
	public static InjectExpressionVisitor EXPRESSION_VISITOR = new InjectExpressionVisitor();
}
