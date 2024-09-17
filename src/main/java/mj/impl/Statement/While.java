package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.BreakException;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Expr;
import mj.impl.Node;
import mj.run.Interpreter;

public class While extends Stat {

    Expr cond;
    Stat stat;
    public While(int line, Expr cond, Stat stat) {
        super(line);
        this.cond = cond;
        this.stat = stat;
    }
    @Override
    public int buildDOTString(StringBuilder sb, String parentName, int count) {
        super.buildDOTString(sb, parentName, count);
        String name = "node%d".formatted(count);
        count = cond.buildDOTString(sb, name, count + 1);
        count = stat.buildDOTString(sb, name, count + 1);
        return count;
    }
    @Override
    public String getName() {
        return "While";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        cond.execute(interpreter);
        int condition = interpreter.pop();
        while (condition == 1) {
            try {
                stat.execute(interpreter);
            }
            catch (BreakException ex) {
                break;
            }
            cond.execute(interpreter);
            condition = interpreter.pop();
        }
    }
    @Override
    public TreeItem<Node> buildTreeView() {
        TreeItem<Node> item = super.buildTreeView();
        item.getChildren().add(cond.buildTreeView());
        item.getChildren().add(stat.buildTreeView());
        return item;
    }
}
