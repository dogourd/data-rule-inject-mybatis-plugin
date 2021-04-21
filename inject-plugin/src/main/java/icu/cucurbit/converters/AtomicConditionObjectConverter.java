package icu.cucurbit.converters;

import icu.cucurbit.rule.AtomicConditionObject;
import icu.cucurbit.rule.ConditionObject;
import icu.cucurbit.sql.TableRule;

public abstract class AtomicConditionObjectConverter implements ConditionObjectConverter{

    @Override
    public ConditionObject convert(TableRule tableRule) {
        return convertToAtomicObject(tableRule);
    }

    public abstract AtomicConditionObject convertToAtomicObject(TableRule tableRule);
}
