package icu.cucurbit.rule.handlers;

import com.google.common.collect.Sets;
import icu.cucurbit.rule.AtomicConditionObject;

import java.util.*;

public class CompareConditionHandler extends AtomicConditionHandler {

    private static final Set<String> SUPPORT_OPERATORS = Sets.newHashSet(
            ">", ">=", "=", "<=", "<", "<>", "!="
    );


    @Override
    public boolean support(AtomicConditionObject conditionObject) {
        if (!Optional.ofNullable(conditionObject).map(AtomicConditionObject::getOperator).isPresent()) {
            return false;
        }

        String operator = conditionObject.getOperator().trim();

        return SUPPORT_OPERATORS.contains(operator);
    }

    @Override
    public String genSqlSnippet(AtomicConditionObject conditionObject) {
        Objects.requireNonNull(conditionObject);
        Objects.requireNonNull(conditionObject.getField());
        Objects.requireNonNull(conditionObject.getOperator());
        Objects.requireNonNull(conditionObject.getValue());

        return  conditionObject.getField() + " " +
                conditionObject.getOperator() + " ? ";
    }

    @Override
    public List<Object> extractParams(AtomicConditionObject conditionObject) {
        if (!(conditionObject.getValue() instanceof Comparable)) {
            throw new IllegalArgumentException("ComparableCondition require a comparable value.");
        }
        return Collections.singletonList(conditionObject.getValue());
    }
}
