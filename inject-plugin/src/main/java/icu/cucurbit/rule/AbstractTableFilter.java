package icu.cucurbit.rule;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.type.TypeHandlerRegistry;

public abstract class AbstractTableFilter {

	protected String table;

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public abstract String toSqlExpression(Connection connection, TypeHandlerRegistry typeHandlerRegistry) throws SQLException;

}
