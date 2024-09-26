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
    public void toDOTString(StringBuilder sb, String parentName) {
        super.toDOTString(sb, parentName);
        cond.toDOTString(sb, dotId);
        stat.toDOTString(sb, dotId);
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
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(cond.toTreeView());
        item.getChildren().add(stat.toTreeView());
        return item;
    }
}
