package icu.cucurbit.sql.filter;

import com.google.common.collect.Sets;
import icu.cucurbit.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class LikeFilter extends Filter {

    private final String field;
    private final String operator;
    private final String value;

    public static final Set<String> LIKE_RELATIONS = Collections.unmodifiableSet(Sets.newHashSet(
            "LIKE", "NOT LIKE"
    ));

    public LikeFilter(Object obj) {
        Objects.requireNonNull(obj);
        List<Object> list = castAsList(obj);
        if (list.size() != 3) {
            log.error("like filter expect 3 params but got [{}] params", list.size());
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }
        String operator = Optional.ofNullable(list.get(0)).map(String::valueOf).map(String::toUpperCase)
                .orElseThrow(() -> new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR));
        String field = Optional.ofNullable(list.get(1)).map(String::valueOf)
                .orElseThrow(() -> new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR));
        String value = Optional.ofNullable(list.get(2)).map(String::valueOf)
                .orElseThrow(() -> new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR));

        if (!LIKE_RELATIONS.contains(operator)) {
            log.error("like filter unknown operator: [{}]", operator);
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }
        this.operator = operator;
        this.field = field;
        this.value = value;
    }


    @Override
    public String toSqlSnippet(Table table, Supplier<String> placeHolderSupplier) {
        return table.getSchemaName() + "." + table.getName() + "." + this.field + " " +
                this.operator + " " +
                placeHolderSupplier.get();
    }

    @Override
    public List<Object> getParameters() {
        return Collections.singletonList(this.value);
    }
}
