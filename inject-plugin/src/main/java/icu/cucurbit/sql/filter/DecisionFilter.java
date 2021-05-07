package icu.cucurbit.sql.filter;

import com.google.common.collect.Sets;
import icu.cucurbit.bo.FilterTable;
import icu.cucurbit.sql.TableRule;

import java.util.*;
import java.util.function.Supplier;

public class DecisionFilter extends RuleFilter {

    public static final Set<String> DECISION_RELATIONS = Sets.newHashSet(
            "<", "<=", "=", ">=", ">", "<>", "!="
    );


    private final String field;
    private final String relation;
    private final List<Object> jdbcParameters;

    public DecisionFilter(TableRule tableRule) {
        Objects.requireNonNull(tableRule);
        Objects.requireNonNull(tableRule.getTableName());
        Objects.requireNonNull(tableRule.getField());
        Objects.requireNonNull(tableRule.getRelation());
        Objects.requireNonNull(tableRule.getTarget());

        this.filterTable = new FilterTable(tableRule.getTableName(), tableRule.getTableName());
        this.field = tableRule.getField();
        String relation = tableRule.getRelation().trim().toUpperCase();
        if (!DECISION_RELATIONS.contains(relation)) {
            throw new IllegalArgumentException("cannot create DecisionFilter, relation [" + relation + "] not support");
        }
        this.relation = relation;
        Object target = tableRule.getTarget();
        if (!(target instanceof Comparable)) {
            throw new IllegalArgumentException("DecisionFilter require a comparable value, but got ["
                    + target.getClass().getName() + "]");
        }
        this.jdbcParameters = new ArrayList<>(1);
        this.jdbcParameters.add(target);
    }

    @Override
    public String toSqlSnippet(FilterTable filterTable, Supplier<String> placeHolderSupplier) {
        Objects.requireNonNull(placeHolderSupplier);

        filterTable = Optional.ofNullable(filterTable).orElse(this.filterTable);
        String tableName = Optional.ofNullable(filterTable.getAlias()).orElse(filterTable.getName());
        return tableName + "." + field + " " + relation + " " + placeHolderSupplier.get();
    }

    @Override
    public List<Object> getJdbcParameters() {
        return jdbcParameters;
    }

}
