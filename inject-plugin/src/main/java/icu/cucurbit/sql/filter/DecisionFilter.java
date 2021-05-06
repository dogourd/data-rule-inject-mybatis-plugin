package icu.cucurbit.sql.filter;

import com.google.common.collect.Sets;
import icu.cucurbit.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class DecisionFilter extends Filter {

    private final String operator;
    private final String field;
    private final Object value;

    public static final Set<String> DECISION_RELATIONS = Collections.unmodifiableSet(Sets.newHashSet(
            "<", "<=", "=", ">=", ">", "<>", "!="
    ));

    public DecisionFilter(Object obj) {
        Objects.requireNonNull(obj);
        List<Object> list = castAsList(obj);
        if (list.size() != 3) {
            log.error("decision filter require 3 params but got [{}]", list.size());
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }
        String operator = Optional.ofNullable(list.get(0)).map(String::valueOf).map(String::toUpperCase).orElseThrow(
                () -> new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR));
        String field = Optional.ofNullable(list.get(1)).map(String::valueOf).orElseThrow(
                () -> new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR));
        Object value = Optional.ofNullable(list.get(2)).orElseThrow(() -> new RuntimeException("value must not null"));
        if (!DECISION_RELATIONS.contains(operator)) {
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }
        if (!(value instanceof Comparable)) {
            log.error("decision filter expect comparable value but got: [{}]", value.getClass().getName());
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }

        this.field = field;
        this.operator = operator;
        this.value = value;

    }

    @Override
    public String toSqlSnippet(Table table, Supplier<String> placeHolderSupplier) {
        return table.getSchemaName() + "." + table.getName() +
                "." + this.field + " " + this.operator +
                placeHolderSupplier.get();
    }

    @Override
    public List<Object> getParameters() {
        return Collections.singletonList(value);
    }
}
