package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Designator;
import mj.impl.Expr.Expr;
import mj.impl.Node;
import mj.run.Interpreter;

public class Assignment extends Stat {

    Designator var;
    Expr expr;

    public Assignment(int line, Designator var, Expr expr) {
        super(line);
        this.var = var;
        this.expr = expr;
    }
    @Override
    public int buildDOTString(StringBuilder sb, String parentName, int count) {
        super.buildDOTString(sb, parentName, count);
        String name = "node%d".formatted(count);
        count = var.buildDOTString(sb, name, count + 1);
        count = expr.buildDOTString(sb, name, count + 1);
        return count;
    }
    @Override
    public String getName() {
        return "Assignment";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        expr.execute(interpreter);
        int val = interpreter.pop();
        interpreter.assign(var, val);
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(var.toTreeView());
        item.getChildren().add(expr.toTreeView());
        return item;
    }
}
