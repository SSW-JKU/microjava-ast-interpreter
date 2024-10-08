package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Exceptions.ReturnException;
import mj.impl.Expr.Expr;
import mj.impl.Node;
import mj.run.Interpreter;

public class Return extends Stat {
    private final Expr expr;
    public Return(int line, Expr expr) {
        super(line);
        this.expr = expr;
    }
    public Return(int line) {
        this(line, null);
    }
    @Override
    public void toDOTString(StringBuilder sb, String parentName) {
        super.toDOTString(sb, parentName);
        if (expr != null) {
            expr.toDOTString(sb, dotId);
        }
    }
    @Override
    public String getName() {
        return "Return";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        if (expr != null) {
            expr.execute(interpreter);
        }
        throw new ReturnException();
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        if (expr != null) {
            item.getChildren().add(expr.toTreeView());
        }
        return item;
    }
}
