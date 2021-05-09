package icu.cucurbit.sql.modifier;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

public class StatementHandlerModifier {

	private final MetaObject statementHandler;

	StatementHandlerModifier(MetaObject statementHandler) {
		this.statementHandler = statementHandler;
	}


	public ParameterHandler parameterHandler() {
		return get("parameterHandler");
	}

	public Executor executor() {
		return get("executor");
	}

	public BoundSqlModifier newBoundSqlModifier() {
		return new BoundSqlModifier(boundSql());
	}

	public BoundSql boundSql() {
		return get("boundSql");
	}

	public MappedStatement mappedStatement() {
		return get("mappedStatement");
	}

	public Configuration configuration() {
		return get("configuration");
	}

	@SuppressWarnings("unchecked")
	private <T> T get(String property) {
		return (T) statementHandler.getValue(property);
	}
}
