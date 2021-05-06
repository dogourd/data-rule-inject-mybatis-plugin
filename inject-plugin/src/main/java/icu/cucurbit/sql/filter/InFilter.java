package icu.cucurbit.sql.filter;

import com.google.common.collect.Sets;
import icu.cucurbit.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
@Slf4j
public class InFilter extends Filter {

    private final String operator;
    private final String field;
    private final List<Object> inContent;

    public static final Set<String> IN_RELATIONS = Collections.unmodifiableSet(Sets.newHashSet(
            "IN", "NOT IN"
    ));

    public InFilter(Object obj) {
        Objects.requireNonNull(obj);
        List<Object> list = castAsList(obj);
        if (list.size() != 3) {
            log.error("in filter require 3 params but got [{}] params", list.size());
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }
        String operator = Optional.ofNullable(list.get(0)).map(String::valueOf).map(String::toUpperCase)
                .orElseThrow(() -> new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR));
        String field = Optional.ofNullable(list.get(1)).map(String::valueOf)
                .orElseThrow(() -> new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR));
        List<Object> inContent = castAsList(list.get(2));
        if (!IN_RELATIONS.contains(operator)) {
            log.error("in filter unknown operator: [{}]", operator);
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }
        if (inContent == null || inContent.isEmpty()) {
            log.error("in value cannot be empty");
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }
        this.operator = operator;
        this.field = field;
        this.inContent = inContent;
    }

    @Override
    public String toSqlSnippet(Table table, Supplier<String> placeHolderSupplier) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(table.getSchemaName()).append(".")
                .append(table.getName()).append(".")
                .append(this.field).append(" ")
                .append(this.operator).append(" ");

        StringJoiner joiner = new StringJoiner(",", "(", ")");
        this.inContent.forEach(v -> joiner.add(placeHolderSupplier.get()));
        sqlBuilder.append(joiner);
        return sqlBuilder.toString();
    }

    @Override
    public List<Object> getParameters() {
        return this.inContent;
    }
}
