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
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(var.toTreeView());
        return item;
    }
}
