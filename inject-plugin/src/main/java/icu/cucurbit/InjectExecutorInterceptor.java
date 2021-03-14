package icu.cucurbit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import icu.cucurbit.rule.TableRule;
import icu.cucurbit.sql.visitor.InjectSelectVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.WithItem;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
public class InjectExecutorInterceptor implements Interceptor {

    private static final String INJECT_SUFFIX = "_inject_rule";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];

        BoundSql boundSql;
        if (args.length == 4) {
            boundSql = ms.getBoundSql(parameter);
        } else {
            boundSql = (BoundSql) args[5];
        }

//        List<TableRule> rules = InjectContext.getFilters();
		List<TableRule> rules = new ArrayList<>();
        if (Objects.nonNull(rules) && !rules.isEmpty()) {
            InjectSelectVisitor injectVisitor = new InjectSelectVisitor();
            String sql = boundSql.getSql();
            Statement statement = CCJSqlParserUtil.parse(sql);

            // 修改 sql.
            injectSelectSql(statement, injectVisitor);

            String newSql = statement.toString();
            BoundSql newBoundSql = copyBoundSql(newSql, ms.getConfiguration(), boundSql);

            String newMsId = ms.getId() + INJECT_SUFFIX;
            MappedStatement newStatement = copyMappedStatement(ms, new DirectSqlSource(newBoundSql), newMsId);

            // 更换方法参数.
            args[0] = newStatement;
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }


    private void injectSelectSql(Statement statement, InjectSelectVisitor injectVisitor) {
        Select select = (Select) statement;
        // with.
        List<WithItem> withItems = select.getWithItemsList();
        if (Objects.nonNull(withItems) && !withItems.isEmpty()) {
            for (WithItem withItem : withItems) {
                withItem.accept(injectVisitor);
            }
        }

        // select.
        SelectBody selectBody = select.getSelectBody();
        selectBody.accept(injectVisitor);
    }


    private MappedStatement copyMappedStatement(MappedStatement oldMs, SqlSource newSqlSource, String newMsId) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
                oldMs.getConfiguration(), newMsId, newSqlSource, oldMs.getSqlCommandType()
        );
        builder.resource(oldMs.getResource());
        builder.fetchSize(oldMs.getFetchSize());
        builder.statementType(oldMs.getStatementType());
        builder.keyGenerator(oldMs.getKeyGenerator());
        builder.keyProperty(oldMs.getKeyProperties() == null ? null : String.join(",", oldMs.getKeyProperties()));
        builder.timeout(oldMs.getTimeout());
        builder.parameterMap(oldMs.getParameterMap());
        builder.resultMaps(oldMs.getResultMaps());
        builder.resultSetType(oldMs.getResultSetType());
        builder.cache(oldMs.getCache());
        builder.flushCacheRequired(oldMs.isFlushCacheRequired());
        builder.useCache(oldMs.isUseCache());

        return builder.build();
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

    public static class DirectSqlSource implements SqlSource {

        private final BoundSql boundSql;

        public DirectSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object o) {
            return boundSql;
        }
    }
}
