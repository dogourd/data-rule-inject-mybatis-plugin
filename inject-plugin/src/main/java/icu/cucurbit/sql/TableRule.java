package icu.cucurbit.sql;

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
}
