package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.impl.Tab;
import mj.run.Interpreter;
import mj.symtab.Struct;

public class NewArray extends Expr {
    Ident ident;
    Expr expr;

    public NewArray(int line, Ident ident, Expr expr) {
        super(line, ident==null? Tab.noType:new Struct(ident.type), 0, Kind.Con);
        this.ident = ident;
        this.expr = expr;
    }
    @Override
    public int toDOTString(StringBuilder sb, String parentName, int count) {
        super.toDOTString(sb, parentName, count);
        String name = "node%d".formatted(count);
        count = ident.toDOTString(sb, name, count + 1);
        count = expr.toDOTString(sb, name, count + 1);
        return count;
    }
    @Override
    public String getName() {
        return "NewArray";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);

        expr.execute(interpreter);
        int len = interpreter.pop();
        int adr = interpreter.alloc(len + 1);
        interpreter.setHeap(adr, len);
        interpreter.push(adr + 1);
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(ident.toTreeView());
        item.getChildren().add(expr.toTreeView());
        return item;
    }
}
