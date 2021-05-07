package icu.cucurbit.sql.filter;


import icu.cucurbit.bo.FilterTable;

import java.util.List;
import java.util.function.Supplier;

public abstract class RuleFilter {

    protected FilterTable filterTable;

    public FilterTable getTable() {
        return filterTable;
    }



    public abstract String toSqlSnippet(FilterTable filterTable, Supplier<String> placeHolderSupplier);

    public abstract List<Object> getJdbcParameters();

}
