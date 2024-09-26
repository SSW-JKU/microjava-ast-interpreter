package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.impl.Tab;
import mj.run.Interpreter;

public class New extends Expr {
    private final Ident ident;
    public New(int line, Ident ident) {
        super(line, ident==null? Tab.noType:ident.type, 0, Kind.Con);
        this.ident = ident;
    }
    @Override
    public void toDOTString(StringBuilder sb, String parentName) {
        super.toDOTString(sb, parentName);
        ident.toDOTString(sb, dotId);
    }
    @Override
    public String getName() {
        return "New";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        int adr = interpreter.alloc(ident.getObj().type.nrFields());
        interpreter.push(adr);
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(ident.toTreeView());
        return item;
    }
}
