package icu.cucurbit.rule;

import com.google.common.collect.Lists;
import icu.cucurbit.rule.handlers.*;

import java.util.List;
import java.util.StringJoiner;

public class ConditionManager {
    private static List<AtomicConditionHandler> handlers = Lists.newArrayList(
           new CompareConditionHandler(),
           new InConditionHandler(),
           new IsConditionHandler(),
           new LikeConditionHandler()
    );

    public static String parse(String logic, AtomicConditionObject... objects) {
        StringJoiner result = new StringJoiner(" " + logic + " ", "(", ")");
        for (AtomicConditionObject object : objects) {
            StringBuilder prefix = new StringBuilder();
            parse(object, prefix);
            String str = prefix.toString();
            result.add(str);
        }
        return result.toString();
    }

    public static StringBuilder parse(AtomicConditionObject conditionObject) {
        StringBuilder prefix = new StringBuilder("(");
        parse(conditionObject, prefix);
        prefix.append(")");
        return prefix;
    }
    public static StringBuilder parse(AtomicConditionObject conditionObject, StringBuilder builder) {
        if (conditionObject == null) {
            return builder;
        }
        for (AtomicConditionHandler handler : handlers) {
            if (handler.support(conditionObject)) {
                String sqlSnippet = handler.genSqlSnippet(conditionObject);
                builder.append(sqlSnippet);
            }
        }
        return builder;
    }

    public static void main(String[] args) {

    }
}
