package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.impl.Obj;
import mj.impl.Tab;
import mj.run.Interpreter;

public class New extends Expr {

    Obj obj;

    public New(int line, Obj obj) {
        super(line, obj==null? Tab.noType:obj.type, 0, Kind.Con);
        this.obj = obj;
    }
    @Override
    public int buildDOTString(StringBuilder sb, String parentName, int count) {
        super.buildDOTString(sb, parentName, count);
        String name = "node%d".formatted(count);
        count = obj.buildDOTString(sb, name, count + 1);
        return count;
    }
    @Override
    public String getName() {
        return "New";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        int adr = interpreter.alloc(obj.type.nrFields());
        interpreter.push(adr);
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(obj.toTreeView());
        return item;
    }
}
