package icu.cucurbit.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * relation should be: like, not like
 * value should be a string.
 * */
public class LikeFilter extends AbstractTableFilter {

	private String field;
	private String relation;
	private String value;

	public LikeFilter(String field, String relation, String value) {
		this.field = field;
		this.relation = relation;
		this.value = value;
	}

	@Override
	public String toSqlExpression(Connection connection, TypeHandlerRegistry typeHandlerRegistry) throws SQLException {

		String prepareSql = getTable() + "." + field + " " + relation + " " + "?";
		PreparedStatement preparedStatement = connection.prepareStatement(prepareSql);

		TypeHandler<String> typeHandler = typeHandlerRegistry.getTypeHandler(String.class);
		typeHandler.setParameter(preparedStatement, 1, value, null);

		return preparedStatement.toString();
	}
}
