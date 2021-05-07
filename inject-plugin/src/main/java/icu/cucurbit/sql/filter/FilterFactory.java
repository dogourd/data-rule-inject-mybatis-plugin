package icu.cucurbit.sql.filter;

import icu.cucurbit.sql.TableRule;

import java.util.Objects;

public class FilterFactory {

    public static RuleFilter create(TableRule rule) {
        Objects.requireNonNull(rule);
        Objects.requireNonNull(rule.getRelation());

        String relation = rule.getRelation().trim().toUpperCase();
        if (DecisionFilter.DECISION_RELATIONS.contains(relation)) {
            return new DecisionFilter(rule);
        } else if (LikeFilter.LIKE_RELATIONS.contains(relation)) {
            return new LikeFilter(rule);
        } else if (InFilter.IN_RELATIONS.contains(relation)) {
            return new InFilter(rule);
        } else if (BetweenFilter.BETWEEN_RELATIONS.contains(relation)) {
            return new BetweenFilter(rule);
        } else if (IsFilter.IS_RELATIONS.contains(relation)) {
            return new IsFilter(rule);
        } else {
            throw new IllegalArgumentException("relation [" + relation + "] not support.");
        }
    }
}
