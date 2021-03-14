package icu.cucurbit.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * relation should be: in, not in
 * values should not empty;
 * */
public class InFilter extends AbstractTableFilter {

	private String field;
	private String relation;
	private List<Object> values;

	public InFilter(String field, String relation, List<Object> values) {
		this.field = field;
		this.relation = relation;
		this.values = values;
	}

	@Override
	public String toSqlExpression(Connection connection, TypeHandlerRegistry typeHandlerRegistry) throws SQLException {
		StringBuilder builder = new StringBuilder();
		builder.append(getTable()).append(".").append(field).append(" ").append(relation).append("( ");
		for (int i = 0; i < values.size(); i++) {
			builder.append("?,");
		}
		builder.delete(builder.length() - 1, builder.length());
		builder.append(")");

		String prepareSql = builder.toString();
		PreparedStatement preparedStatement = connection.prepareStatement(prepareSql);

		for (int i = 0; i < values.size(); i++) {
			Object item = values.get(i);
			Class<?> javaType = item.getClass();
			TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(javaType);
			typeHandler.setParameter(preparedStatement, i, item, null);
		}

		return preparedStatement.toString();
	}
}
