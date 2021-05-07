package icu.cucurbit.sql.filter;

import com.google.common.collect.Sets;
import icu.cucurbit.bo.FilterTable;
import icu.cucurbit.sql.TableRule;

import java.util.*;
import java.util.function.Supplier;

public class IsFilter extends RuleFilter {

    public static final Set<String> IS_RELATIONS = Sets.newHashSet("IS NULL", "IS NOT NULL");

    private final String field;
    private final String relation;

    public IsFilter(TableRule rule) {
        Objects.requireNonNull(rule);
        Objects.requireNonNull(rule.getTableName());
        Objects.requireNonNull(rule.getField());
        Objects.requireNonNull(rule.getRelation());

        this.filterTable = new FilterTable(rule.getTableName(), rule.getTableName());
        this.field = rule.getField();
        String relation = rule.getRelation().trim().toUpperCase();
        if (!IS_RELATIONS.contains(relation)) {
            throw new IllegalArgumentException("cannot create IsFilter, relation [" + relation + "] not support.");
        }
        this.relation = relation;
        // ignore target.
    }

    @Override
    public String toSqlSnippet(FilterTable filterTable, Supplier<String> placeHolderSupplier) {
        // ignore placeHolderSupplier.
        filterTable = Optional.ofNullable(filterTable).orElse(this.filterTable);
        String tableName = Optional.ofNullable(filterTable.getAlias()).orElse(filterTable.getName());
        return tableName + "." + field + " " + relation + " NULL";
    }

    @Override
    public List<Object> getJdbcParameters() {
        return Collections.emptyList();
    }
}
