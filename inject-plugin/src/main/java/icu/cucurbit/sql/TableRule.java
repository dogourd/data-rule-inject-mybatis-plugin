package icu.cucurbit.sql;

import icu.cucurbit.TableRuleHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRule {

    private String tableName;
    private String property;
    private String relation;
    private Object target;


    public String toExpressionString() {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(property);
        Objects.requireNonNull(relation);
        Objects.requireNonNull(target);


        return tableName + "." + TableRuleHelper.toSql(this);
    }

}
