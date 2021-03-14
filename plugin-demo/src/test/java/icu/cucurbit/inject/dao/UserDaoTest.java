package icu.cucurbit.inject.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import icu.cucurbit.InjectContext;
import icu.cucurbit.inject.entity.User;
import icu.cucurbit.rule.AbstractTableFilter;
import icu.cucurbit.rule.FilterFactory;
import icu.cucurbit.rule.TableRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.postgresql.jdbc.PgConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserDaoTest {

	@Autowired
	private DataSource dataSource;
	@Autowired
	private UserDao userDao;

	@Before
	public void setup() {
		List<TableRule> rules = new ArrayList<>();
		rules.add(new TableRule("users", "username", "=", "jaaaar"));

		List<AbstractTableFilter> filters = rules.stream().map(FilterFactory::createFilter).collect(Collectors.toList());
		InjectContext.setFilters(filters);
	}

	@Test
	public void testFindById() {
		List<User> users = userDao.findAllById("id");
		log.info("{}", users);
	}

	@Test
	public void testFindByIdIn() {
		List<String> ids = Arrays.asList("1", "2", "3", "4");
		List<User> users = userDao.findAllByIdIn(ids);
		log.info("{}", users);
	}

	@Test
	public void testUnwrap() throws SQLException {
		System.out.println(dataSource.unwrap(PgConnection.class));
	}
}
