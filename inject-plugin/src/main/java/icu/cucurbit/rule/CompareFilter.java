package icu.cucurbit.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * relation should be: >, >=, =, <=, <, !=, <>
 * */
public class CompareFilter extends AbstractTableFilter {

	private String field;
	private String relation;
	private Object value;

	public CompareFilter(String field, String relation, Object value) {
		this.field = field;
		this.relation = relation;
		this.value = value;
	}

	@Override
	public String toSqlExpression(Connection connection, TypeHandlerRegistry typeHandlerRegistry) throws SQLException {
		StringBuilder builder = new StringBuilder();
		builder.append(getTable()).append(".").append(field).append(" ").append(relation).append(" ").append("?");

		String prepareSql = builder.toString();
		PreparedStatement preparedStatement = connection.prepareStatement(prepareSql);

		Class<?> javaType = value.getClass();
		TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(javaType);
		typeHandler.setParameter(preparedStatement, 1, value, null);
		return preparedStatement.toString();
	}
}
