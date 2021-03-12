package icu.cucurbit.rule.handlers;

import icu.cucurbit.rule.ConditionObject;

import java.util.List;

public interface ConditionHandler {

    boolean support(ConditionObject conditionObject);
    String genSqlSnippet(ConditionObject conditionObject);
    List<Object> extractParams(ConditionObject conditionObject);
}
