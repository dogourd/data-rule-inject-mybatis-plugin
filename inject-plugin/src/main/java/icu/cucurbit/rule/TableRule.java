package icu.cucurbit.rule;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRule {

    private String tableName;
    private String field;
    private String relation;
    private Object target;



    public String toExpressionString() {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(field);
        Objects.requireNonNull(relation);
        Objects.requireNonNull(target);


        StringBuilder builder = new StringBuilder();
        builder.append(tableName).append(".").append(field).append(relation);
        if (target instanceof String) {
            builder.append("'").append(target).append("'");
        } else {
            builder.append(target);
        }
        return builder.toString();
    }

}
