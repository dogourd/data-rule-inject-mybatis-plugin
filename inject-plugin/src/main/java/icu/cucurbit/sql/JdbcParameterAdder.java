package icu.cucurbit.sql;

import java.util.HashMap;
import java.util.Map;

public class IndexObjectMapping {

    private int index;
    private Map<Integer, Object> mapping = new HashMap<>();

    public void add(Object value) {
        if (mapping.containsKey(index)) {
            throw new IllegalArgumentException("index " + index + " already use");
        }
        mapping.put(index, value);
    }

    public Map<Integer, Object> getMapping() {
        return mapping;
    }

    private void skipIndex() {
        index ++;
    }
}
