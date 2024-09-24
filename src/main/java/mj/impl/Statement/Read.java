package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Expr.Designator;
import mj.impl.Node;

public abstract class Read extends Stat {

    Designator var;

    public Read(int line, Designator var) {
        super(line);
        this.var = var;
    }
    @Override
    public int toDOTString(StringBuilder sb, String parentName, int count) {
        super.toDOTString(sb, parentName, count);
        String name = "node%d".formatted(count);
        count = var.toDOTString(sb, name, count + 1);
        return count;
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(var.toTreeView());
        return item;
    }
}
