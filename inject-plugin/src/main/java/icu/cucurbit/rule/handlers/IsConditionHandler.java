package icu.cucurbit.rule.handlers;

import icu.cucurbit.rule.AtomicConditionObject;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class IsConditionHandler extends AtomicConditionHandler {

    private static final String IS_OPERATOR = "is";

    @Override
    public boolean support(AtomicConditionObject conditionObject) {
        if (!Optional.ofNullable(conditionObject).map(AtomicConditionObject::getOperator).isPresent()) {
            return false;
        }

        return IS_OPERATOR.equalsIgnoreCase(conditionObject.getOperator().trim());
    }

    @Override
    public String genSqlSnippet(AtomicConditionObject conditionObject) {
        Objects.requireNonNull(conditionObject);
        Objects.requireNonNull(conditionObject.getField());
        Objects.requireNonNull(conditionObject.getOperator());
        Objects.requireNonNull(conditionObject.getValue());

        NullEnum nullEnum;
        if (!(conditionObject.getValue() instanceof NullEnum)) {
            String str = conditionObject.getValue().toString();
            nullEnum = NullEnum.of(str);
            if (Objects.isNull(nullEnum)) {
                throw new IllegalArgumentException("IsCondition require a NullEnum value.");
            }
        } else {
            nullEnum = (NullEnum) conditionObject.getValue();
        }

        return
                conditionObject.getField() + " " +
                conditionObject.getOperator() + " " +
                (nullEnum == NullEnum.NULL ? "null" : "not null") ;
    }

    @Override
    public List<Object> extractParams(AtomicConditionObject conditionObject) {
        return Collections.emptyList();
    }

    @Getter
    public enum NullEnum {
        NULL,
        NOTNULL,
        ;

        public static NullEnum of(String code) {
            Objects.requireNonNull(code);
            return Stream.of(values())
                    .filter(bean -> bean.name().equalsIgnoreCase(code))
                    .findAny().orElse(null);
        }
    }
}
