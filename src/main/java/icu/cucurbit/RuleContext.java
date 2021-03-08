package icu.cucurbit;


import icu.cucurbit.sql.TableRule;

import java.util.ArrayList;
import java.util.List;

public class RuleContext {

    private static final ThreadLocal<List<TableRule>> tableRuleThreadLocal =
            ThreadLocal.withInitial(ArrayList::new);

    public static void setRules(List<TableRule> rules) {
        tableRuleThreadLocal.set(rules);
    }

    public static List<TableRule> getRules() {
        return tableRuleThreadLocal.get();
    }

    public static void clear() {
        tableRuleThreadLocal.remove();
    }
}
