package icu.cucurbit.converters;

import icu.cucurbit.rule.ConditionObject;
import icu.cucurbit.sql.TableRule;

public interface ConditionObjectConverter {

    boolean supportParameter(TableRule tableRule);

    ConditionObject convert(TableRule tableRule);
}
