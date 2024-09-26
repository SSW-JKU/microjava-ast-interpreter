package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.run.Interpreter;

import java.util.ArrayList;
import java.util.List;

public class Block extends Stat {

    List<Stat> statList;

    public Block(int line) {
        super(line);
        statList = new ArrayList<>();
    }
    @Override
    public void toDOTString(StringBuilder sb, String parentName) {
        super.toDOTString(sb, parentName);
        for (Stat stat : statList) {
            stat.toDOTString(sb, dotId);
        }
    }
    @Override
    public String getName() {
        return "Block";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        for (Node node : statList) {
            node.execute(interpreter);
        }
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        for (Node node : statList) {
            item.getChildren().add(node.toTreeView());
        }
        return  item;
    }
    public void add(Stat node) {
        statList.add(node);
    }
}
