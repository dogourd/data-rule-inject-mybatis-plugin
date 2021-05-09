package icu.cucurbit;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import icu.cucurbit.sql.JdbcIndexAndParameters;
import icu.cucurbit.sql.TableRule;
import icu.cucurbit.sql.filter.FilterFactory;
import icu.cucurbit.sql.filter.RuleFilter;
import icu.cucurbit.sql.visitor.InjectCrudVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class InjectDeleteTest {

    @Before
    public void setup() {
        List<TableRule> rules = Lists.newArrayList(
                new TableRule("county", "code", "=", "countyCode"),
                new TableRule("city", "code", "in", Lists.newArrayList("1101", "1102"))
        );
        List<RuleFilter> filters = new ArrayList<>(rules.size());

        rules.forEach(rule -> filters.add(FilterFactory.create(rule)));
        FilterContext.setFilters(filters);
    }

    @Test
    public void testSimpleDelete() throws JSQLParserException {
        String sql = "delete from county";
        String newSql = inject(sql);
        Assert.assertEquals("DELETE FROM county WHERE county.code = ?", newSql);
    }

    @Test
    public void testWhereDelete() throws JSQLParserException {
        String sql = "delete from county where \"length\"(county.code) > 6";
        String newSql = inject(sql);
        System.out.println(newSql);
//        Assert.assertEquals("DELETE FROM county WHERE \"length\"(county.code) > 6 AND county.code = 'countyCode'", newSql);
    }

    @Test
    public void testWhereJoin() throws JSQLParserException {
        String sql = "delete from county join (select * from city where city.code = ?) city on county.parent_code = city.code where \"length\"(county.code) > ?";
        String newSql = inject(sql);
        System.out.println(newSql);
    }

    private String inject(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);

        JdbcIndexAndParameters parameterAdder = new JdbcIndexAndParameters();
        InjectCrudVisitor crudVisitor = new InjectCrudVisitor(parameterAdder);

        statement.accept(crudVisitor);
        System.out.println(parameterAdder.getMapping());

        return statement.toString();

    }
}
