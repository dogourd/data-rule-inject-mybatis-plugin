package icu.cucurbit.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcIndexAndParameters  {

    private int index;
    private final Map<Integer, Object> mapping = new HashMap<>();

    public void skipOne() {
        index ++;
    }

    public void addParameter(List<Object> parameters) {
        parameters.forEach(this::addSingleParameter);
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
