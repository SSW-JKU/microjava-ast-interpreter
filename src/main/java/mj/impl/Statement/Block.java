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
    public int buildDOTString(StringBuilder sb, String parentName, int count) {
        super.buildDOTString(sb, parentName, count);
        String name = "node%d".formatted(count);
        for (Stat stat : statList) {
            count = stat.buildDOTString(sb, name, count + 1);
        }
        return count;
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
    public TreeItem<Node> buildTreeView() {
        TreeItem<Node> item = super.buildTreeView();
        for (Node node : statList) {
            item.getChildren().add(node.buildTreeView());
        }
        return  item;
    }
    public void add(Stat node) {
        statList.add(node);
    }
}
