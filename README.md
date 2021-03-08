## mybatis 规则条件注入插件  
向执行的SQL查询语句中拼接where语句(and条件), 如果存在pageHelper插件,需要去除pageHelper-starter,
手动方式配置mybatis插件, 并在添加PageInterceptor之后添加InjectInterceptor.