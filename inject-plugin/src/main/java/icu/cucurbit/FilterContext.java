package icu.cucurbit;


import icu.cucurbit.sql.filter.RuleFilter;

import java.util.ArrayList;
import java.util.List;

public class FilterContext {

    private static final ThreadLocal<List<RuleFilter>> tableFilters =
            ThreadLocal.withInitial(ArrayList::new);

    public static void setFilters(List<RuleFilter> rules) {
        tableFilters.set(rules);
    }

    public static List<RuleFilter> getFilters() {
        return tableFilters.get();
    }

    public static void clear() {
        tableFilters.remove();
    }
}
