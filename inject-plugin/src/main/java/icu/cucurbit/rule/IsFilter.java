package icu.cucurbit.rule;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * relation should be: is, is not
 * */
public class IsFilter extends AbstractTableFilter {

	private String field;
	private String relation;

	public IsFilter(String field, String relation) {
		this.field = field;
		this.relation = relation;
	}

	@Override
	public String toSqlExpression(Connection connection, TypeHandlerRegistry typeHandlerRegistry) throws SQLException {
		return getTable() + "." + field + " " + relation + " " + "null";
	}
}
