package icu.cucurbit;

import icu.cucurbit.sql.TableRule;
import icu.cucurbit.sql.visitor.InjectSelectVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
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
        RuleContext.setRules(rules);
    }

    @Test
    public void testSimpleSql() throws JSQLParserException {
        String sql = "select * from users";
        injectAndPrintSql(sql, rules);
    }

    @Test
    public void testJoinSql() throws JSQLParserException {
        String sql = "select * from users join user_role on users.id = user_role.user_id";
        injectAndPrintSql(sql, rules);
    }

    @Test
    public void testUnionAllSql() throws JSQLParserException {
        String sql = "select * from users where id = 1 union all select * from users where id = 2";
        injectAndPrintSql(sql, rules);
    }

    @Test
    public void testWithSql() throws JSQLParserException {
        String sql = "with \"tmp\" as (select * from users) select * from tmp where tmp.id = 1";
        injectAndPrintSql(sql, rules);
    }

    @Test
    public void testSubQuerySql() throws JSQLParserException {
        String sql = "select * from (select * from users) u";
        injectAndPrintSql(sql, rules);
    }

    @Test
    public void testAlias() throws JSQLParserException {
        String sql = "select * from users u";
        injectAndPrintSql(sql, rules);
    }

    @Test
    public void testWhereSubQuery() throws JSQLParserException {
        String sql = "select * from user_role where user_id in (select * from users)";
        injectAndPrintSql(sql, rules);
    }





    private void injectAndPrintSql(String selectSql, List<TableRule> rules) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(selectSql);
        Select select = (Select) statement;

        InjectSelectVisitor visitor = new InjectSelectVisitor();

        List<WithItem> withItems = select.getWithItemsList();
        if (withItems != null && !withItems.isEmpty()) {
            for (WithItem item : withItems) {
                item.accept(visitor);
            }
        }

        select.getSelectBody().accept(visitor);

        System.out.println(statement.toString());
    }
}
