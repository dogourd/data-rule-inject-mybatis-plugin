package icu.cucurbit;

import icu.cucurbit.sql.JdbcIndexAndParameters;
import icu.cucurbit.sql.TableRule;
import icu.cucurbit.sql.filter.FilterFactory;
import icu.cucurbit.sql.filter.RuleFilter;
import icu.cucurbit.sql.visitor.InjectCrudVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(BlockJUnit4ClassRunner.class)
public class InjectSelectTest {


    private List<TableRule> rules;

    @Before
    public void setup() {
        rules = new ArrayList<>();
        rules.add(new TableRule("users", "del_flag", "=", 0));
        rules.add(new TableRule("user_role", "id", "=", 0));
        List<RuleFilter> filters = new ArrayList<>(rules.size());

        rules.forEach(rule -> filters.add(FilterFactory.create(rule)));
        FilterContext.setFilters(filters);
    }

    @Test
    public void testSimpleSql() throws JSQLParserException {
        String sql = "select * from users";
        String newSql = inject(sql);
        System.out.println(newSql);
//        Assert.assertEquals("SELECT * FROM users WHERE users.del_flag = 0", newSql);
    }

    @Test
    public void testJoinSql() throws JSQLParserException {
        String sql = "select * from users join user_role on users.id = user_role.user_id";
        String newSql = inject(sql);
        System.out.println(newSql);
//        Assert.assertEquals("SELECT * FROM users JOIN user_role ON users.id = user_role.user_id WHERE users.del_flag = 0 AND user_role.id = 0", newSql);
    }

    @Test
    public void testUnionAllSql() throws JSQLParserException {
        String sql = "select * from users where id = 1 union all select * from users where id = 2";
        String newSql = inject(sql);
        System.out.println(newSql);
//        Assert.assertEquals("SELECT * FROM users WHERE id = 1 AND users.del_flag = 0 UNION ALL SELECT * FROM users WHERE id = 2 AND users.del_flag = 0", newSql);
    }

    @Test
    public void testWithSql() throws JSQLParserException {
        String sql = "with \"tmp\" as (select * from users where del_flag = ?) select * from tmp where tmp.id = ?";
        String newSql = inject(sql);
        System.out.println(newSql);
//        Assert.assertEquals("WITH \"tmp\" AS (SELECT * FROM users WHERE users.del_flag = 0) SELECT * FROM tmp WHERE tmp.id = 1", newSql);
    }

    @Test
    public void testSubQuerySql() throws JSQLParserException {
        String sql = "select * from (select * from users) u";
        String newSql = inject(sql);
        System.out.println(newSql);
//        Assert.assertEquals("SELECT * FROM (SELECT * FROM users WHERE users.del_flag = 0) u", newSql);
    }

    @Test
    public void testAlias() throws JSQLParserException {
        String sql = "select * from users u";
        String newSql = inject(sql);
        System.out.println(newSql);
//        Assert.assertEquals("SELECT * FROM users u WHERE u.del_flag = 0", newSql);
    }

    @Test
    public void testWhereSubQuery() throws JSQLParserException {
        String sql = "select * from user_role where role_id = ? and user_id in (select * from users where users.id = ?)";
        String newSql = inject(sql);
        System.out.println(newSql);
//        Assert.assertEquals("SELECT * FROM user_role WHERE user_id IN (SELECT * FROM users WHERE users.del_flag = 0) AND user_role.id = 0", newSql);
    }





    private String inject(String selectSql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(selectSql);
        JdbcIndexAndParameters parameterAdder = new JdbcIndexAndParameters();
        InjectCrudVisitor v2 = new InjectCrudVisitor(parameterAdder);
        statement.accept(v2);
        System.out.println(parameterAdder.getMapping());
        return statement.toString();
    }
}
