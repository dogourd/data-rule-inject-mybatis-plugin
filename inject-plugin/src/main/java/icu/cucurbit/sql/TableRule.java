package icu.cucurbit.sql;

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


        StringBuilder builder = new StringBuilder();
        builder.append(tableName).append(".").append(property).append(relation);
        if (target instanceof String) {
            builder.append("'").append(target).append("'");
        } else {
            builder.append(target);
        }
        return builder.toString();
    }

}
