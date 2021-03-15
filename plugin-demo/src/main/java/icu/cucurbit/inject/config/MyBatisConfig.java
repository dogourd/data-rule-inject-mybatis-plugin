package icu.cucurbit.inject.config;

import icu.cucurbit.InjectExecutorInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
public class MyBatisConfig {

    private final List<SqlSessionFactory> sqlSessionFactories;

    public MyBatisConfig(List<SqlSessionFactory> sqlSessionFactories) {
        this.sqlSessionFactories = sqlSessionFactories;
    }


    @PostConstruct
    public void addInterceptor() {
        InjectExecutorInterceptor interceptor = new InjectExecutorInterceptor();
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
            // 如果需要 pageHelper 在 InjectExecutorInterceptor 之前 add.

            sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
        }
    }
}
