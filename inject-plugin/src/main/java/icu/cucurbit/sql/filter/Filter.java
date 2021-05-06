package icu.cucurbit.sql.filter;

import icu.cucurbit.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public abstract class Filter {



    public abstract String toSqlSnippet(Table table, Supplier<String> placeHolderSupplier);

    public abstract List<Object> getParameters();



    protected List<Object> castAsList(Object obj) {
        if (obj == null) {
            return Collections.emptyList();
        }
        List<Object> objs = new ArrayList<>();
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                objs.add(Array.get(obj, i));
            }
            return objs;
        } else if (obj instanceof Collection) {
            objs.addAll((Collection<?>) obj);
            return objs;
        }
        log.error("expect collection but got : [{}]", obj.getClass().getName());
        throw new RuntimeException(Constants.ErrMsg.REQUEST_PARAM_ERROR);
    }


}
