package icu.cucurbit.rule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AtomicConditionObject extends ConditionObject {

    private String field;
    private String operator;
    private Object value;


    public String toString() {
        return "(" + field + operator + value + ")";
    }


    @Override
    protected String toSql() {
        return "(" + field + operator + value + ")";
    }
}
