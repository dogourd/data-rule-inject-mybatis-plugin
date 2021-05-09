package icu.cucurbit;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import icu.cucurbit.sql.JdbcIndexAndParameters;
import icu.cucurbit.sql.filter.RuleFilter;
import icu.cucurbit.sql.modifier.BoundSqlModifier;
import icu.cucurbit.sql.modifier.PluginUtils;
import icu.cucurbit.sql.modifier.StatementHandlerModifier;
import icu.cucurbit.sql.visitor.InjectCrudVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;

@Intercepts({
		@Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class })
})
public class InjectStatementInterceptor implements Interceptor {
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		// 第一版，测试用
		Object target = invocation.getTarget();
		StatementHandler statementHandler = (StatementHandler) target;

		StatementHandlerModifier statementModifier = PluginUtils.newStatementHandlerModifier(statementHandler);
		BoundSqlModifier boundSqlModifier = statementModifier.newBoundSqlModifier();
		Configuration configuration = statementModifier.configuration();

		List<RuleFilter> filters = FilterContext.getFilters();
		if (filters == null || filters.isEmpty()) { // 没有规则.
			return invocation.proceed();
		}

		// 根据 DataScopes 进行数据权限的 sql 处理
		Statements statements = CCJSqlParserUtil.parseStatements(boundSqlModifier.sql());
		for (Statement statement : statements.getStatements()) {
			JdbcIndexAndParameters indexAndParameters = new JdbcIndexAndParameters();
			InjectCrudVisitor visitor = new InjectCrudVisitor(indexAndParameters);

			List<ParameterMapping> parameterMappings = boundSqlModifier.parameterMappings();

			statement.accept(visitor);
			String injectedSql = statement.toString();
			boundSqlModifier.withSql(injectedSql);

			indexAndParameters.getMapping().forEach((index, value) -> {
				String parameterName = "drp_" + index;
				boundSqlModifier.addAdditionalParameter("drp_" + index, value);
				ParameterMapping.Builder mappingBuilder = new ParameterMapping.Builder(
						configuration, parameterName , value.getClass()
				);
				parameterMappings.add(index, mappingBuilder.build());
			});
			boundSqlModifier.withParameterMappings(parameterMappings);
		}

		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		if (target instanceof StatementHandler) {
			return Plugin.wrap(target, this);
		}
		return target;
	}

	@Override
	public void setProperties(Properties properties) {
		Interceptor.super.setProperties(properties);
	}
}
