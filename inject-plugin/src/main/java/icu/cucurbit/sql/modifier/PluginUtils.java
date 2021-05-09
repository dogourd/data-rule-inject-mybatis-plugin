package icu.cucurbit.sql.modifier;

import java.lang.reflect.Proxy;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

public class PluginUtils {

	public static final String DELEGATE_BOUNDSQL_SQL = "delegate.boundSql.sql";

	/**
	 * 获得真正的处理对象,可能多层代理.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T realTarget(Object target) {
		if (Proxy.isProxyClass(target.getClass())) {
			MetaObject metaObject = SystemMetaObject.forObject(target);
			return realTarget(metaObject.getValue("h.target"));
		}
		return (T) target;
	}


	public static BoundSqlModifier newBoundSqlModifier(BoundSql boundSql) {
		return new BoundSqlModifier(boundSql);
	}

	public static StatementHandlerModifier newStatementHandlerModifier(StatementHandler statementHandler) {
		statementHandler = realTarget(statementHandler);
		MetaObject object = SystemMetaObject.forObject(statementHandler);
		return new StatementHandlerModifier(SystemMetaObject.forObject(object.getValue("delegate")));
	}
}
