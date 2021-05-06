package icu.cucurbit;


import icu.cucurbit.sql.TableRule;

import java.util.ArrayList;
import java.util.List;

public class RuleContext {

    private static final ThreadLocal<List<TableRule>> tableFilters =
            ThreadLocal.withInitial(ArrayList::new);

    public static void setRules(List<TableRule> rules) {
        tableFilters.set(rules);
    }

    public static List<TableRule> getRules() {
        return tableFilters.get();
    }

    public static void clear() {
        tableFilters.remove();
    }
}
