package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Expr;
import mj.impl.Node;
import mj.run.Interpreter;

public abstract class Print extends Stat {

    Expr expr;
    int width;
    int val;
    char ch;

    public Print(int line, Expr expr, int width) {
        super(line);
        this.expr = expr;
        this.width = width;
    }
    public Print(int line, Expr expr) {
        this(line, expr, 0);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        expr.execute(interpreter);
        val = interpreter.pop();
        ch = (char)val;
        for (int i = 0; i < width - String.valueOf(val).length(); i++) {
            System.out.print(" ");
        }
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(expr.toTreeView());
        return item;
    }
}
