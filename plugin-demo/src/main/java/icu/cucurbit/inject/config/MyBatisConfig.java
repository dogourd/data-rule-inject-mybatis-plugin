package icu.cucurbit.inject.config;

import java.util.List;

import javax.annotation.PostConstruct;

import icu.cucurbit.InjectStatementInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {

    private final List<SqlSessionFactory> sqlSessionFactories;

    public MyBatisConfig(List<SqlSessionFactory> sqlSessionFactories) {
        this.sqlSessionFactories = sqlSessionFactories;
    }


    @PostConstruct
    public void addInterceptor() {
//        InjectExecutorInterceptor interceptor = new InjectExecutorInterceptor();
		InjectStatementInterceptor interceptor = new InjectStatementInterceptor();
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
            // 如果需要 pageHelper 在 InjectExecutorInterceptor 之前 add.
            sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
        }
    }
}
