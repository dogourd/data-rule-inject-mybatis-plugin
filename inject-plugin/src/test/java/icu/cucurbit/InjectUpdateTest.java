package icu.cucurbit;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class InjectUpdateTest {

	@Test
	public void testSimpleUpdate() throws JSQLParserException {
		String sql = "update users set username = 'jaaaar' where id = 1";
		Statement statement = CCJSqlParserUtil.parse(sql);
		Update update = (Update) statement;

		System.out.println(update.getTables());    // [users]
		System.out.println(update.getSelect());     // null
		System.out.println(update.getExpressions()); // [jaaaar]
		System.out.println(update.getWhere()); // id = 1
		System.out.println(update.getColumns()); // [username]
		System.out.println(update.getFromItem()); // null
		System.out.println(update.getJoins()); // null

	}
}
