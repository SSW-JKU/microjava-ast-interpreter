package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.impl.Operator;
import mj.impl.Tab;
import mj.run.Interpreter;

public class UnaryExpr extends Expr {

    Expr expr;
    Operator op;

    public UnaryExpr(int line, Operator op, Expr expr) {
        super(line, Tab.intType, 0, Kind.Con);
        this.op = op;
        this.expr = expr;
    }
    @Override
    public void toDOTString(StringBuilder sb, String parentName) {
        super.toDOTString(sb, parentName);
        expr.toDOTString(sb, dotId);
    }
    @Override
    public String getName() {
        return "UnaryExpr (%s)".formatted(op);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        expr.execute(interpreter);
        interpreter.push(-interpreter.pop());
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(expr.toTreeView());
        return item;
    }
}
