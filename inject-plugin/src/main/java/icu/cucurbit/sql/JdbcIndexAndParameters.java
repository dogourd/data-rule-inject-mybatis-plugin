package icu.cucurbit.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JdbcIndexAndParameters  {

    private int index;
    private int nextIndex;
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
        if (index < nextIndex) {
            index = nextIndex;
        }
        if (mapping.containsKey(index)) {
            throw new IllegalArgumentException("index " + index + " already use");
        }
        mapping.put(index, value);
        nextIndex = index + 1;
    }

    public Map<Integer, Object> getMapping() {
        return mapping;
    }

}
