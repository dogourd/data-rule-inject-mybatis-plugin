## mybatis 规则条件注入插件  
动态向MyBatis执行的SQL语句中添加WHERE语句(AND条件)。  
提供Executor及StatementHandler两种形式插件。
- Executor(**InjectExecutorInterceptor**)  
    修改MappedStatementId, 方便在日志中找到注入数据规则的SQL语句, 但可能与pagehelper-starter冲突, 需手动配置插件顺序，确保在添加
  **PageInterceptor**之后再添加 **InjectExecutorInterceptor**.
- StatementHandler (**InjectStatementInterceptor**)  
    不会修改MappedStatementId, 不会与 pagehelper 冲突.