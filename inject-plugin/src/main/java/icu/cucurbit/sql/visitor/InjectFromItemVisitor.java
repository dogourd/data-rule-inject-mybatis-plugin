package icu.cucurbit.sql.visitor;

import java.util.Objects;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesisFromItem;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;

public class InjectFromItemVisitor implements FromItemVisitor {

    private Table table;

    public InjectFromItemVisitor() {

    }

    @Override
    public void visit(Table table) {
        this.table = table;
	}

    @Override
    public void visit(SubSelect subSelect) {
        SelectBody selectBody = subSelect.getSelectBody();
        selectBody.accept(InjectVisitors.SELECT_VISITOR);
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


    public boolean foundTable() {
        return Objects.nonNull(table);
    }

    public Table getTable() {
        return table;
    }
}
