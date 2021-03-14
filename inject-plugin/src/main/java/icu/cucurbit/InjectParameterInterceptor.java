package icu.cucurbit;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import icu.cucurbit.sql.visitor.InjectSelectVisitor;
import icu.cucurbit.sql.visitor.InjectVisitors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.WithItem;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.postgresql.jdbc.PgConnection;

@Intercepts({
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class})
})
public class InjectParameterInterceptor implements Interceptor {

	private static Field configurationField;
	private static Field typeHandlerRegistryField;
	private static Field boundSqlField;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
		DefaultParameterHandler parameterHandler = (DefaultParameterHandler) invocation.getTarget();

		Configuration configuration = (Configuration) configurationField.get(parameterHandler);
		TypeHandlerRegistry registry = (TypeHandlerRegistry) typeHandlerRegistryField.get(parameterHandler);
		BoundSql boundSql = (BoundSql) boundSqlField.get(parameterHandler);
		PreparedStatement preparedStatement = (PreparedStatement) invocation.getArgs()[0];
		Connection connection = preparedStatement.getConnection();
		Connection pgConnection = connection.unwrap(PgConnection.class);

		InjectContext.setConnection(pgConnection);
		InjectContext.setRegistry(registry);


		String sql = boundSql.getSql();
		String newSql = inject(sql);

		BoundSql newBoundSql = copyBoundSql(newSql, configuration, boundSql);
		boundSqlField.set(parameterHandler, newBoundSql);

		return invocation.proceed();
    }


    private String inject(String sql) throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(sql);
		InjectSelectVisitor selectVisitor = InjectVisitors.SELECT_VISITOR;

		Select select = (Select) statement;
		// with.
		List<WithItem> withItems = select.getWithItemsList();
		if (Objects.nonNull(withItems) && !withItems.isEmpty()) {
			for (WithItem withItem : withItems) {
				withItem.accept(selectVisitor);
			}
		}

		// select.
		SelectBody selectBody = select.getSelectBody();
		selectBody.accept(selectVisitor);

		return statement.toString();
	}

	private BoundSql copyBoundSql(String newSql, Configuration configuration, BoundSql oldBoundSql) throws NoSuchFieldException, IllegalAccessException {

		BoundSql newBoundSql = new BoundSql(
				configuration, newSql, oldBoundSql.getParameterMappings(), oldBoundSql.getParameterObject()
		);
		// reflect copy additionalParameters and metaParameters

		Class<? extends BoundSql> clz = oldBoundSql.getClass();

		Field additionalParametersField = clz.getDeclaredField("additionalParameters");
		additionalParametersField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Object> additionalParameters = (Map<String, Object>) additionalParametersField.get(oldBoundSql);
		additionalParameters.forEach(newBoundSql::setAdditionalParameter);

		return newBoundSql;
	}

    static {
		try {
			configurationField = DefaultParameterHandler.class.getDeclaredField("configuration");
			typeHandlerRegistryField = DefaultParameterHandler.class.getDeclaredField("typeHandlerRegistry");
			boundSqlField = DefaultParameterHandler.class.getDeclaredField("boundSql");

			configurationField.setAccessible(true);
			typeHandlerRegistryField.setAccessible(true);
			boundSqlField.setAccessible(true);
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException("get field configuration|typeHandlerRegistry fail.", e);
		}
	}
}
