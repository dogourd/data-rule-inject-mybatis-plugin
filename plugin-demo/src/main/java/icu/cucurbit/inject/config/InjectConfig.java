package icu.cucurbit.inject.config;

import java.util.List;

import javax.annotation.PostConstruct;

import icu.cucurbit.InjectParameterInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;

import org.springframework.context.annotation.Configuration;

@Configuration
public class InjectConfig {

	private final List<SqlSessionFactory> sqlSessionFactories;

	public InjectConfig(List<SqlSessionFactory> sqlSessionFactories) {
		this.sqlSessionFactories = sqlSessionFactories;
	}

	@PostConstruct
	public void addInterceptor() {
//		InjectExecutorInterceptor interceptor = new InjectExecutorInterceptor();
		InjectParameterInterceptor interceptor = new InjectParameterInterceptor();
		for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
			sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
		}
	}
}
