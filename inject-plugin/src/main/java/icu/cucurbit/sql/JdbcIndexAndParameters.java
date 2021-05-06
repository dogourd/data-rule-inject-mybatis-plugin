package icu.cucurbit.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JdbcIndexAndParameters  {

    private int index;
    private final Map<Integer, Object> mapping = new HashMap<>();

    public void skipOne() {
        index ++;
    }

    public void addParameter(Object value) {
        if (value instanceof Collection) {
            ((Collection<?>) value).forEach(this::addSingleParameter);
        } else {
            addSingleParameter(value);
        }
    }

    public void addSingleParameter(Object value) {
        if (mapping.containsKey(index)) {
            throw new IllegalArgumentException("index " + index + " already use");
        }
        mapping.put(index ++, value);
    }

    public Map<Integer, Object> getMapping() {
        return mapping;
    }

}
