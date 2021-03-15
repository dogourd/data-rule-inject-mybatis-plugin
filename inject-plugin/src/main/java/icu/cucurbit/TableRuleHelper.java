package icu.cucurbit;

import com.google.common.collect.Sets;
import icu.cucurbit.sql.TableRule;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TableRuleHelper {

    private static final Set<String> IN_RELATIONS = Sets.newHashSet("IN", "NOT IN");
    private static final Set<String> BETWEEN_RELATIONS = Sets.newHashSet("BETWEEN", "NOT BETWEEN");
    private static final Set<String> LIKE_RELATIONS = Sets.newHashSet("LIKE", "NOT LIKE");
    private static final Set<String> COMPARE_RELATIONS = Sets.newHashSet(">", ">=", "=", "<=", "<", "<>");
    private static final Set<String> IS_RELATIONS = Sets.newHashSet("IS NULL", "IS NOT NULL");

    public static String toSql(TableRule tableRule) {
        Objects.requireNonNull(tableRule);
        Objects.requireNonNull(tableRule.getRelation());
        Objects.requireNonNull(tableRule.getProperty());

        String relation = tableRule.getRelation().trim().toUpperCase(Locale.ENGLISH);

        // in
        if (IN_RELATIONS.contains(relation)) {
            if (!(tableRule.getTarget() instanceof Iterable)) {
                throw new IllegalArgumentException(tableRule.getTarget() + " is not an iterable value.");
            }
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append(tableRule.getProperty()).append(" ").append(relation).append(" (");
            StringJoiner commaJoiner = new StringJoiner(",");
            ((Iterable<?>) tableRule.getTarget()).forEach(item -> commaJoiner.add(value(item)));
            sqlBuilder.append(commaJoiner.toString());
            sqlBuilder.append(")");
            return sqlBuilder.toString();
        }
        // is
        if (IS_RELATIONS.contains(relation)) {
            return tableRule.getProperty() + " " + relation;
        }
        // like
        if (LIKE_RELATIONS.contains(relation)) {
            if (!(tableRule.getTarget() instanceof String)) {
                throw new IllegalArgumentException(tableRule.getTarget() + " is not a string value.");
            }
            return tableRule.getProperty() + " " + relation + " " + value(tableRule.getTarget());
        }
        // >, =....
        if (COMPARE_RELATIONS.contains(relation)) {
            return tableRule.getProperty() + " " + relation + " " + value(tableRule.getTarget());
        }
        // between
        if (BETWEEN_RELATIONS.contains(relation)) {
            Object min, max;
            if (tableRule.getTarget().getClass().isArray()) {
                min = Array.get(tableRule.getTarget(), 0);
                max = Array.get(tableRule.getTarget(), 1);
            } else if (tableRule.getTarget() instanceof List) {
                min = ((List) tableRule.getTarget()).get(0);
                max = ((List) tableRule.getTarget()).get(1);
            } else {
                throw new IllegalArgumentException(tableRule.getTarget() + " parse fail.");
            }

            return tableRule.getProperty() + " " + relation + " " + value(min) + " AND " + value(max);
        }
        throw new IllegalArgumentException("cannot parse tableRule: " + tableRule);
    }




    private static String value(Object value) {
        if (value instanceof String) {
            return "'" + ((String) value).replace("'", "''") + "'";
        }
        if (value instanceof Date) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return value(formatter.format((Date) value));
        }
        if (value instanceof LocalDateTime) {
            return value(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format((LocalDateTime) value));
        }
        return value.toString();
    }


}
