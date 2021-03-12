package icu.cucurbit.rule.handlers;

import icu.cucurbit.rule.AtomicConditionObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LikeConditionHandler extends AtomicConditionHandler {

    private static final String LIKE_OPERATOR = "like";

    @Override
    public boolean support(AtomicConditionObject conditionObject) {
        if (!Optional.ofNullable(conditionObject).map(AtomicConditionObject::getOperator).isPresent()) {
            return false;
        }

        return LIKE_OPERATOR.equalsIgnoreCase(conditionObject.getOperator().trim());
    }

    @Override
    public String genSqlSnippet(AtomicConditionObject conditionObject) {
        Objects.requireNonNull(conditionObject);
        Objects.requireNonNull(conditionObject.getField());
        Objects.requireNonNull(conditionObject.getOperator());
        Objects.requireNonNull(conditionObject.getValue());

        return
                conditionObject.getField() + " "
                + conditionObject.getOperator() + " ? ";
    }

    @Override
    public List<Object> extractParams(AtomicConditionObject conditionObject) {
        return Collections.singletonList(conditionObject.getValue());
    }
}
