package icu.cucurbit.sql.filter;

import java.util.List;

public abstract class Filter {

    public abstract List<Object> jdbcParameters();
    public abstract String toSqlSnippet();
}
