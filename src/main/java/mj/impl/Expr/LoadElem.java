package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.impl.Tab;
import mj.run.Interpreter;

public class LoadElem extends Expr {
    private final Expr expr;
    public LoadElem(int line, Expr expr) {
        super(line, expr==null? Tab.noType:expr.type, 0, Expr.Kind.None);
        this.expr = expr;
    }
    public LoadElem(int line) {
        this(line,null);
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
        return "LoadElem";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);

        if (expr != null) {
            expr.execute(interpreter);
        }
        int idx = interpreter.pop();
        int adr = interpreter.pop();
        interpreter.push(interpreter.getHeap(adr + idx));
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
