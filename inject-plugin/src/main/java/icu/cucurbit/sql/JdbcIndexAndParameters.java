package icu.cucurbit.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JdbcIndexAndParameters  {

    private int index;
    private int nextIndex;
    private Map<Integer, Object> mapping = new HashMap<>();

    public void skipOne() {
        index ++;
    }

    public void addParameter(Object value) {
        if (value instanceof Collection) {
            addParameters((Collection<Object>) value);
            return;
        }
        if (index < nextIndex) {
            index = nextIndex;
        }
        if (mapping.containsKey(index)) {
            throw new IllegalArgumentException("index " + index + " already use");
        }
        mapping.put(index, value);
        nextIndex = index + 1;
    }

    public void addParameters(Collection<Object> values) {
        if (index < nextIndex) {
            index = nextIndex;
        }
        if (mapping.containsKey(index)) {
            throw new IllegalArgumentException("index " + index + " already use");
        }
        int curIndex = index;
        for (Object value : values) {
            mapping.put(curIndex ++, value);
        }
        nextIndex = curIndex;
    }

    public Map<Integer, Object> getMapping() {
        return mapping;
    }

}
