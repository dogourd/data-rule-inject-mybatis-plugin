package icu.cucurbit.sql.filter;

import com.google.common.collect.Sets;
import icu.cucurbit.bo.FilterTable;
import icu.cucurbit.sql.TableRule;

import java.util.*;
import java.util.function.Supplier;

public class InFilter extends RuleFilter {

    public static final Set<String> IN_RELATIONS = Sets.newHashSet("IN", "NOT IN");

    private final String field;
    private final String relation;
    private final List<Object> jdbcParameters;

    public InFilter(TableRule rule) {
        Objects.requireNonNull(rule);
        Objects.requireNonNull(rule.getTableName());
        Objects.requireNonNull(rule.getField());
        Objects.requireNonNull(rule.getRelation());
        Objects.requireNonNull(rule.getTarget());

        this.filterTable = new FilterTable(rule.getTableName(), rule.getTableName());
        this.field = rule.getField();
        String relation = rule.getRelation().trim().toUpperCase();
        if (!IN_RELATIONS.contains(relation)) {
            throw new IllegalArgumentException("cannot create InFilter, relation [" + relation + "] not support.");
        }
        this.relation = relation;
        Object target = rule.getTarget();
        this.jdbcParameters = new ArrayList<>();
        if (target instanceof Iterable) {
            ((Iterable<?>) target).forEach(jdbcParameters::add);
        } else {
            jdbcParameters.add(target);
        }
    }

    @Override
    public String toSqlSnippet(FilterTable filterTable, Supplier<String> placeHolderSupplier) {
        Objects.requireNonNull(placeHolderSupplier);

        filterTable = Optional.ofNullable(filterTable).orElse(this.filterTable);
        String tableName = Optional.ofNullable(filterTable.getAlias()).orElse(filterTable.getName());
        StringBuilder sqlBuilder = new StringBuilder(tableName + "." + field + " " + relation + " ");
        StringJoiner parameterJoiner = new StringJoiner(",", "(", ")");
        jdbcParameters.forEach(p -> parameterJoiner.add(placeHolderSupplier.get()));
        sqlBuilder.append(parameterJoiner);
        return sqlBuilder.toString();
    }

    @Override
    public List<Object> getJdbcParameters() {
        return jdbcParameters;
    }
}
