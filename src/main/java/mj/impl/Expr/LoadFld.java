package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.impl.Tab;
import mj.run.Interpreter;

public class LoadFld extends Expr {

    Expr expr;

    public LoadFld(int line, Expr expr, int adr) {
        super(line, expr==null? Tab.noType:expr.type, adr, Kind.None);
        this.expr = expr;
    }
    public LoadFld(int line, int adr) {
        this(line, null, adr);
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
        return "LoadFld";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);

        if (expr != null) {
            expr.execute(interpreter);
        }
        int adr = interpreter.pop();
        interpreter.push(interpreter.getHeap(adr + offset));
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
