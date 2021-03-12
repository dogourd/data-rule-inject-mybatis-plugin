package icu.cucurbit.rule.handlers;


import icu.cucurbit.rule.AtomicConditionObject;
import icu.cucurbit.rule.ConditionObject;

import java.util.List;

public abstract class AtomicConditionHandler implements ConditionHandler {

    @Override
    public boolean support(ConditionObject conditionObject) {
        return conditionObject instanceof AtomicConditionObject
                && support((AtomicConditionObject) conditionObject);
    }

    @Override
    public String genSqlSnippet(ConditionObject conditionObject) {
        return genSqlSnippet((AtomicConditionObject) conditionObject);
    }

    @Override
    public List<Object> extractParams(ConditionObject conditionObject) {
        return extractParams((AtomicConditionObject) conditionObject);
    }

    abstract boolean support(AtomicConditionObject conditionObject);

    abstract String genSqlSnippet(AtomicConditionObject conditionObject);

    abstract List<Object> extractParams(AtomicConditionObject conditionObject);
}
