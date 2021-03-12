package icu.cucurbit.sql.visitor;

import icu.cucurbit.sql.TableRule;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;

public class InjectCrudVisitor extends StatementVisitorAdapter {

    private final List<TableRule> rules;

    public InjectCrudVisitor(List<TableRule> rules) {
        this.rules = rules;
    }

    @Override
    public void visit(Delete delete) {

    }

    @Override
    public void visit(Update update) {
        Select select = update.getSelect();
        // users del_flag = 0

        //
        // select * from user_role where user_id in (select user_id from users) and user_role.id = 0;
    }

    @Override
    public void visit(Insert insert) {

    }

    @Override
    public void visit(Select select) {

    }


}
