package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.run.Interpreter;

public class ElseIf extends Stat {
    If if_;
    Stat else_;

    public ElseIf(int line, If if_, Stat else_) {
        super(line);
        this.if_ = if_;
        this.else_ = else_;
    }
    @Override
    public void toDOTString(StringBuilder sb, String parentName) {
        super.toDOTString(sb, parentName);
        if_.toDOTString(sb, dotId);
        else_.toDOTString(sb, dotId);
    }
    @Override
    public String getName() {
        return "ElseIf";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        if_.executeCond(interpreter);
        int con = interpreter.pop();

        if (con == 1) {
            if_.executeStat(interpreter);
        }
        else {
            else_.execute(interpreter);
        }
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(if_.toTreeView());
        item.getChildren().add(else_.toTreeView());
        return item;
    }
}
