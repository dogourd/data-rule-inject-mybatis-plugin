package icu.cucurbit.rule.handlers;

import com.google.common.collect.Sets;
import icu.cucurbit.rule.AtomicConditionObject;

import java.util.*;

public class InConditionHandler extends AtomicConditionHandler {

    private static final Set<String> IN_OPERATORS = Sets.newHashSet("IN", "NOT IN");

    @Override
    public boolean support(AtomicConditionObject conditionObject) {
        if (!Optional.ofNullable(conditionObject).map(AtomicConditionObject::getOperator).isPresent()) {
            return false;
        }

        return IN_OPERATORS.contains(conditionObject.getOperator().trim().toUpperCase(Locale.ENGLISH));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String genSqlSnippet(AtomicConditionObject conditionObject) {
        Objects.requireNonNull(conditionObject);
        Objects.requireNonNull(conditionObject.getField());
        Objects.requireNonNull(conditionObject.getOperator());
        Objects.requireNonNull(conditionObject.getValue());

        if (!(conditionObject.getValue() instanceof Iterable)) {
            throw new IllegalArgumentException("InCondition require a iterable value.");
        }
        StringBuilder builder = new StringBuilder();
        builder.append(conditionObject.getField()).append(" ")
                .append(conditionObject.getOperator()).append("( ");

        for (Object ignored : (Iterable) conditionObject.getValue()) {
            builder.append("?,");
        }
        builder.replace(builder.length() - 1, builder.length(), ")");

        return builder.toString();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<Object> extractParams(AtomicConditionObject conditionObject) {
        List<Object> params = new ArrayList<>();
        for (Object parameter : (Iterable) conditionObject.getValue()) {
            params.add(parameter);
        }
        return params;
    }
}
