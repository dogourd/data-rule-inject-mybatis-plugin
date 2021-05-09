package icu.cucurbit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import icu.cucurbit.sql.JdbcIndexAndParameters;
import icu.cucurbit.sql.filter.RuleFilter;
import icu.cucurbit.sql.visitor.InjectCrudVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class InjectExecutorInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(InjectExecutorInterceptor.class);

    private static final String INJECT_SUFFIX = "_inject_rule";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        // maybe null, maybe object, maybe MapperMethod$ParamMap,
        Object parameterObject = args[1];
        BoundSql boundSql = args.length == 6 ? (BoundSql) args[5] : mappedStatement.getBoundSql(parameterObject);
        List<RuleFilter> rules = FilterContext.getFilters();
        if (rules == null || rules.isEmpty()) { // 没有配置规则.
            return invocation.proceed();
        }

        String sql = boundSql.getSql();
        Statement statement = CCJSqlParserUtil.parse(sql);
        if (!shouldIntercept(statement)) {
            return invocation.proceed();
        }

        // 修改 sql.
        JdbcIndexAndParameters parameterAdder = new JdbcIndexAndParameters();
        statement.accept(new InjectCrudVisitor(parameterAdder));

        String injectedSql = statement.toString();
        log.debug("inject sql finish. new sql: {}", injectedSql);

        BoundSql injectedBoundSql = copyBoundSql(injectedSql, mappedStatement.getConfiguration(), boundSql);
        List<ParameterMapping> parameterMappings = injectedBoundSql.getParameterMappings();
        parameterAdder.getMapping().forEach((k, v) -> {
        	// drp mean data rule parameter.
            String key = "drp_" + k;
            ParameterMapping.Builder mappingBuilder = new ParameterMapping.Builder(
                    mappedStatement.getConfiguration(), key , v.getClass()
            );
            parameterMappings.add(k, mappingBuilder.build());
            injectedBoundSql.setAdditionalParameter(key, v);
        });

        String newMsId = mappedStatement.getId() + INJECT_SUFFIX;
        MappedStatement newStatement = copyMappedStatement(mappedStatement, new DirectSqlSource(injectedBoundSql), newMsId);
        // 更换方法参数.
        args[0] = newStatement;

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private boolean shouldIntercept(Statement statement) {
        return statement instanceof Select || statement instanceof Update || statement instanceof Delete;
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

        List<ParameterMapping> parameterMappings = oldBoundSql.getParameterMappings().isEmpty()
                ? new ArrayList<>()
                : oldBoundSql.getParameterMappings();
        BoundSql newBoundSql = new BoundSql(
                configuration, newSql, parameterMappings, oldBoundSql.getParameterObject()
        );

        // reflect copy additionalParameters and metaParameters
        Class<? extends BoundSql> clz = oldBoundSql.getClass();

        Field additionalParametersField = clz.getDeclaredField("additionalParameters");
        additionalParametersField.setAccessible(true);
        Object additionalParameters = additionalParametersField.get(oldBoundSql);
        additionalParametersField.set(newBoundSql, additionalParameters);

        Field metaParametersField = clz.getDeclaredField("metaParameters");
        metaParametersField.setAccessible(true);
        Object metaParameters = metaParametersField.get(oldBoundSql);
        metaParametersField.set(newBoundSql, metaParameters);

        return newBoundSql;
    }

    private static class DirectSqlSource implements SqlSource {

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
