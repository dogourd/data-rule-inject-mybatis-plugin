package icu.cucurbit.sql.filter;

import com.google.common.collect.Sets;
import icu.cucurbit.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
@Slf4j
public class GroupFilter extends Filter {

    private final String operator;
    private final List<Filter> filters = new ArrayList<>();

    public static final Set<String> GROUP_RELATIONS = Collections.unmodifiableSet(Sets.newHashSet(
            "AND", "OR"
    ));

    public GroupFilter(Object obj) {
        Objects.requireNonNull(obj);
        if (!(obj instanceof List)) {
            log.error("group filter expect list param but got: [{}]", obj.getClass().getName());
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }
        List<Object> list = (List<Object>) obj;
        if (list.size() <= 2) {
            log.error("group filter expect at least 3 params but got [{}] params", list.size());
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }
        String operator = String.valueOf(list.get(0)).toUpperCase();
        if (!GROUP_RELATIONS.contains(operator)) {
            log.error("group filter unknown operator {}", operator);
            throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
        }
        this.operator = operator;
        for (int i = 1; i < list.size(); i++) {
            Filter filter = FilterFactory.create(list.get(i));
            this.filters.add(filter);
        }
    }


    @Override
    public String toSqlSnippet(Table table, Supplier<String> placeHolderSupplier) {
        StringJoiner joiner = new StringJoiner(" " + this.operator + " ", "(", ")");
        this.filters.forEach(filter -> {
            joiner.add(filter.toSqlSnippet(table, placeHolderSupplier));
        });
        return joiner.toString();
    }

    @Override
    public List<Object> getParameters() {
        List<Object> parameters = new ArrayList<>();
        this.filters.forEach(filter -> parameters.addAll(filter.getParameters()));
        return parameters;
    }
}
