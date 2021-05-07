package icu.cucurbit;

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

import java.util.ArrayList;
import java.util.List;

@RunWith(BlockJUnit4ClassRunner.class)
public class InjectUpdateTest {

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
	public void testSimpleUpdate() throws JSQLParserException {
		String sql = "update county set code = '110001'";
		String newSql = inject(sql);
		Assert.assertEquals("UPDATE county SET code = '110001' WHERE county.code = 'countyCode'", newSql);
	}

	@Test
	public void testUpdateWithFrom() throws JSQLParserException {
		String sql = "update county set code = '110001' from city where county.parent_code = city.code";
		String newSql = inject(sql);
		Assert.assertEquals("UPDATE county SET code = '110001' FROM city WHERE county.parent_code = city.code AND county.code = 'countyCode' AND city.code IN ('1101', '1102')", newSql);
	}

	@Test
	public void testUpdateWithFromAndJoin() throws JSQLParserException {
		String sql = "update county set code = ? from city join province on city.parent_code = province.code where county.parent_code = city.code";
		String newSql = inject(sql);
		System.out.println(newSql);
//		Assert.assertEquals("UPDATE county SET code = '110001' FROM city JOIN province ON city.parent_code = province.code WHERE county.parent_code = city.code AND county.code = 'countyCode' AND city.code IN ('1101', '1102')", newSql);
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
