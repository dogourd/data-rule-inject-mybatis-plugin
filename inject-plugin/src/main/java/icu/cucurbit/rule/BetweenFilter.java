package icu.cucurbit.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * relation should be: between, not between
 * */
public class BetweenFilter extends AbstractTableFilter {

	private String field;
	private String relation;
	private Object left;
	private Object right;

	public BetweenFilter(String field, String relation, Object left, Object right) {
		this.field = field;
		this.relation = relation;
		this.left = left;
		this.right = right;
	}

	@Override
	public String toSqlExpression(Connection connection, TypeHandlerRegistry typeHandlerRegistry) throws SQLException {
		String prepareSql = getTable() + "." + field + " " + relation + " ? AND ?";
		PreparedStatement preparedStatement = connection.prepareStatement(prepareSql);

		Class<?> leftJavaType = left.getClass();
		TypeHandler leftTypeHandler = typeHandlerRegistry.getTypeHandler(leftJavaType);
		leftTypeHandler.setParameter(preparedStatement, 1, left, null);

		Class<?> rightJavaType = right.getClass();
		TypeHandler rightTypeHandler = typeHandlerRegistry.getTypeHandler(rightJavaType);
		rightTypeHandler.setParameter(preparedStatement, 2, rightJavaType, null);

		return preparedStatement.toString();
	}
}
