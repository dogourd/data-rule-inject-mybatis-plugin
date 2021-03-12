package icu.cucurbit.rule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MergedConditionObject extends ConditionObject {

    private String logic;
    private ConditionObject left;
    private ConditionObject right;

    public String toString() {
        return "(" + left.toString() + " " + logic + " " + right.toString() + ")";
    }

    @Override
    protected String toSql() {
        return "(" + left.toString() + " " + logic + " " + right.toString() + ")";
    }
}
