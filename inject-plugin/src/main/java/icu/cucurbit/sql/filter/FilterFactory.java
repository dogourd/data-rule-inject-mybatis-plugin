package icu.cucurbit.sql.filter;

import icu.cucurbit.constants.Constants;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.util.*;

@Slf4j
public class FilterFactory {

    public static Filter create(Object obj) {
        Objects.requireNonNull(obj);

        List<Object> list = castAsList(obj);
        if (list.isEmpty()) {
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }

        String operator = String.valueOf(list.get(0));
        if (isGroupFilter(operator)) {
            return new GroupFilter(obj);
        } else if (isDecisionFilter(operator)) {
            return new DecisionFilter(obj);
        } else if (isLikeFilter(operator)) {
            return new LikeFilter(obj);
        } else if (isInFilter(operator)) {
            return new InFilter(obj);
        }
        log.error("unknown operator: [{}]", operator);
        throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
    }

    private static boolean isGroupFilter(String operator) {
        return GroupFilter.GROUP_RELATIONS.contains(upperCase(operator));
    }
    private static boolean isDecisionFilter(String operator) {
        return DecisionFilter.DECISION_RELATIONS.contains(upperCase(operator));
    }
    private static boolean isLikeFilter(String operator) {
        return LikeFilter.LIKE_RELATIONS.contains(upperCase(operator));
    }
    private static boolean isInFilter(String operator) {
        return InFilter.IN_RELATIONS.contains(upperCase(operator));
    }

    private static List<Object> castAsList(Object obj) {
        if (obj == null) {
            return Collections.emptyList();
        }
        List<Object> objs = new ArrayList<>();
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                objs.add(Array.get(obj, i));
            }
            return objs;
        } else if (obj instanceof Collection) {
            objs.addAll((Collection<?>) obj);
            return objs;
        }
        log.error("expect collection value but got [{}]", obj.getClass().getName());
        throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
    }

    private static String upperCase(String value) {
        return String.valueOf(value).toUpperCase();
    }
}
