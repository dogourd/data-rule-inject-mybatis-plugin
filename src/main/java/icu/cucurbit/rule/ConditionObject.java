package icu.cucurbit.rule;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public abstract class ConditionObject {

    public ConditionObject and(ConditionObject and) {
        return new MergedConditionObject("AND", this, and);
    }

    public ConditionObject or(ConditionObject or) {
        return new MergedConditionObject("OR", this, or);
    }

    protected abstract String toSql();





    public static void main(String[] args) {
        // (a > b) and ( (a > c) or ( (a > d) and (a > e) ) )
        ConditionObject first = new AtomicConditionObject("a", ">", "b");
        ConditionObject second = new AtomicConditionObject("a", ">", "c");
        ConditionObject third = new AtomicConditionObject("a", ">", "d");
        ConditionObject fourth = new AtomicConditionObject("a", ">", "e");

        ConditionObject thirdAndFourth = third.and(fourth);
        ConditionObject secondOrThird = second.or(thirdAndFourth);

        ConditionObject result = first.and(secondOrThird);
        System.out.println(result);
        System.out.println(JSON.toJSONString(result));

        // ( (a > b) and (a > c) ) or ( (a > d) and (a > e) )
        first = new AtomicConditionObject("a", ">", "b");
        second = new AtomicConditionObject("a", ">", "c");
        third = new AtomicConditionObject("a", ">", "d");
        fourth = new AtomicConditionObject("a", ">", "e");
        ConditionObject firstAndSecond = first.and(second);
        ConditionObject thirdAndFourthReNew = third.and(fourth);
        result = firstAndSecond.or(thirdAndFourthReNew);
        System.out.println(result);
        System.out.println(JSON.toJSON(result));


    }
}
