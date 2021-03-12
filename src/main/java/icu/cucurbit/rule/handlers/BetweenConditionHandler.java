package icu.cucurbit.rule.handlers;

import icu.cucurbit.rule.AtomicConditionObject;

import java.util.List;
import java.util.Optional;

public class BetweenConditionHandler extends AtomicConditionHandler {

    private static final String BETWEEN_OPERATOR = "between";

    @Override
    public boolean support(AtomicConditionObject conditionObject) {
        String operator = Optional.ofNullable(conditionObject)
                .map(AtomicConditionObject::getOperator)
                .map(String::trim).orElse("");
        return BETWEEN_OPERATOR.equalsIgnoreCase(operator);
    }

    @Override
    public String genSqlSnippet(AtomicConditionObject conditionObject) {
        return null;
    }

    @Override
    public List<Object> extractParams(AtomicConditionObject conditionObject) {
        return null;
    }
}
