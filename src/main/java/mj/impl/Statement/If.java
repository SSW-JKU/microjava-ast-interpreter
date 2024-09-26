package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Expr;
import mj.impl.Node;
import mj.run.Interpreter;

public class If extends Stat {
    private final Expr cond;
    private final Stat stat;
    public If(int line, Expr cond, Stat stat) {
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
        return "If";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        cond.execute(interpreter);
        int cond = interpreter.pop();
        if (cond == 1) {
            stat.execute(interpreter);
        }
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(cond.toTreeView());
        item.getChildren().add(stat.toTreeView());
        return item;
    }
    public void executeCond(Interpreter interpreter) throws ControlFlowException {
        cond.execute(interpreter);
    }
    public void executeStat(Interpreter interpreter) throws ControlFlowException {
        stat.execute(interpreter);
    }
}
