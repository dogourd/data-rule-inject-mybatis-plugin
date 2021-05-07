package icu.cucurbit.sql.filter;

import com.google.common.collect.Sets;
import icu.cucurbit.bo.FilterTable;
import icu.cucurbit.sql.TableRule;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;

public class BetweenFilter extends RuleFilter {

    public static final Set<String> BETWEEN_RELATIONS = Sets.newHashSet("BETWEEN", "NOT BETWEEN");

    private final String field;
    private final String relation;
    private final List<Object> jdbcParameters;

    public BetweenFilter(TableRule rule) {
        Objects.requireNonNull(rule);
        Objects.requireNonNull(rule.getTableName());
        Objects.requireNonNull(rule.getField());
        Objects.requireNonNull(rule.getRelation());
        Objects.requireNonNull(rule.getTarget());

        this.filterTable = new FilterTable(rule.getTableName(), rule.getTableName());
        this.field = rule.getField();
        String relation = rule.getRelation().trim().toUpperCase();
        if (!BETWEEN_RELATIONS.contains(relation)) {
            throw new IllegalArgumentException("cannot create BetweenFilter, relation [" + relation + "] not support");
        }
        this.relation = relation;


        Object target = rule.getTarget();
        List<Object> parameters = new ArrayList<>();
        // must be iterable or a array.
        Class<?> clz = target.getClass();
        if (target instanceof Iterable) { // is an iterable value.
            Iterator<?> itr = ((Iterable<?>) target).iterator();
            for (int i = 0; i < 2; i++) {
                if (!itr.hasNext()) {
                    throw new IllegalArgumentException("Between Filter require 2 params, but got " + i + " param");
                }
                Object param = itr.next();
                parameters.add(param);
            }
        } else if (clz.isArray()){
            int length = Array.getLength(target);
            if (length < 2) {
                throw new IllegalArgumentException("Between Filter require 2 params, but got " + length + " param");
            }
            parameters.add(Array.get(target, 0));
            parameters.add(Array.get(target, 1));
        } else {
            throw new IllegalArgumentException("BetweenFilter require iterable value or array. but got "
                    + clz.getName());
        }
        this.jdbcParameters = parameters;
    }

    @Override
    public String toSqlSnippet(FilterTable filterTable, Supplier<String> placeHolderSupplier) {
        Objects.requireNonNull(placeHolderSupplier);

        filterTable = Optional.ofNullable(filterTable).orElse(this.filterTable);
        String tableName = Optional.ofNullable(filterTable.getAlias()).orElse(filterTable.getName());

        return tableName + "." + field + " " + relation + " " + placeHolderSupplier.get() + " AND "
                + placeHolderSupplier.get();
    }

    @Override
    public List<Object> getJdbcParameters() {
        return jdbcParameters;
    }
}
