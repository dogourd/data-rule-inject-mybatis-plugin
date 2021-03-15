package icu.cucurbit;

import com.google.common.collect.Lists;
import icu.cucurbit.sql.TableRule;
import icu.cucurbit.sql.visitor.InjectCrudVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;

@RunWith(BlockJUnit4ClassRunner.class)
public class InjectDeleteTest {

    @Before
    public void setup() {
        List<TableRule> rules = Lists.newArrayList(
                new TableRule("county", "code", "=", "countyCode"),
                new TableRule("city", "code", "in", Lists.newArrayList("1101", "1102"))
        );
        RuleContext.setRules(rules);
    }

    @Test
    public void testSimpleDelete() throws JSQLParserException {
        String sql = "delete from county";
        String newSql = inject(sql);
        Assert.assertEquals("DELETE FROM county WHERE county.code = 'countyCode'", newSql);
    }

    @Test
    public void testWhereDelete() throws JSQLParserException {
        String sql = "delete from county where \"length\"(county.code) > 6";
        String newSql = inject(sql);
        Assert.assertEquals("DELETE FROM county WHERE \"length\"(county.code) > 6 AND county.code = 'countyCode'", newSql);
    }

    private String inject(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        InjectCrudVisitor visitor = new InjectCrudVisitor();
        statement.accept(visitor);
        return statement.toString();
    }
}
