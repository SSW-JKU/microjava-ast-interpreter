package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Exceptions.ReturnException;
import mj.impl.Expr.Expr;
import mj.impl.Node;
import mj.run.Interpreter;

public class Return extends Stat {

    Expr expr;

    public Return(int line, Expr expr) {
        super(line);
        this.expr = expr;
    }
    public Return(int line) {
        this(line, null);
    }
    @Override
    public int buildDOTString(StringBuilder sb, String parentName, int count) {
        super.buildDOTString(sb, parentName, count);
        if (expr != null) {
            String name = "node%d".formatted(count);
            count = expr.buildDOTString(sb, name, count + 1);
        }
        return count;
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
    public TreeItem<Node> buildTreeView() {
        TreeItem<Node> item = super.buildTreeView();
        if (expr != null) {
            item.getChildren().add(expr.buildTreeView());
        }
        return item;
    }
}
