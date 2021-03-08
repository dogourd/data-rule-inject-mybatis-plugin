package icu.cucurbit;

import icu.cucurbit.sql.TableRule;
import icu.cucurbit.sql.visitor.InjectTableRuleSelectVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.WithItem;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

@SuppressWarnings("rawtypes")
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
public class InjectInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();

        CacheKey cacheKey;
        BoundSql boundSql;
        if (args.length == 4) {
            boundSql = ms.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
        } else {
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

        List<TableRule> rules = RuleContext.getRules();
        if (Objects.nonNull(rules) && !rules.isEmpty()) {
            InjectTableRuleSelectVisitor injectVisitor = new InjectTableRuleSelectVisitor(rules);

            String sql = boundSql.getSql();
            Statement statement = CCJSqlParserUtil.parse(sql);
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

            String newSql = statement.toString();
            boundSql = new BoundSql(ms.getConfiguration(), newSql,
                    boundSql.getParameterMappings(), boundSql.getParameterObject());

        }

        return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
