package icu.cucurbit.sql.visitor;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.Objects;

public class InjectTableRuleFromItemVisitor implements FromItemVisitor {

    private Table table;
    private final InjectTableRuleSelectVisitor injectTableRuleSelectVisitor;

    public InjectTableRuleFromItemVisitor(InjectTableRuleSelectVisitor selectVisitor) {
        this.injectTableRuleSelectVisitor = selectVisitor;
    }


    @Override
    public void visit(Table tableName) {
        this.table = tableName;
    }

    @Override
    public void visit(SubSelect subSelect) {
        SelectBody selectBody = subSelect.getSelectBody();
        selectBody.accept(injectTableRuleSelectVisitor);
    }

    @Override
    public void visit(SubJoin subjoin) {
        subjoin.accept(this);
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        throw new UnsupportedOperationException("InjectTableRuleFromItemVisitor not support LateralSubSelect.");
    }

    @Override
    public void visit(ValuesList valuesList) {
        throw new UnsupportedOperationException("InjectTableRuleFromItemVisitor not support ValuesList.");
    }

    @Override
    public void visit(TableFunction tableFunction) {
        throw new UnsupportedOperationException("InjectTableRuleFromItemVisitor not support TableFunction.");
    }

    @Override
    public void visit(ParenthesisFromItem aThis) {
        throw new UnsupportedOperationException("InjectTableRuleFromItemVisitor not support ParenthesisFromItem.");
    }


    public boolean findTable() {
        return Objects.nonNull(table);
    }

    public Table getTable() {
        return table;
    }
}
