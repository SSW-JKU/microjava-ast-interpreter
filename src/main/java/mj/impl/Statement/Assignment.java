package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Designator;
import mj.impl.Expr.Expr;
import mj.impl.Node;
import mj.run.Interpreter;

public class Assignment extends Stat {
    private final Designator var;
    private final Expr expr;
    public Assignment(int line, Designator var, Expr expr) {
        super(line);
        this.var = var;
        this.expr = expr;
    }
    @Override
    public void toDOTString(StringBuilder sb, String parentName) {
        super.toDOTString(sb, parentName);
        var.toDOTString(sb, dotId);
        expr.toDOTString(sb, dotId);
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
        if (var.kind == Expr.Kind.Elem || var.kind == Expr.Kind.Fld) {
            var.execute(interpreter);
        }
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
