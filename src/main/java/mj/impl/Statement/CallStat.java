package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Call;
import mj.impl.Expr.Designator;
import mj.impl.Expr.Expr;
import mj.impl.Node;
import mj.impl.Obj;
import mj.impl.Tab;
import mj.run.Interpreter;

import java.util.List;

public class CallStat extends Stat {
    Call methodToCall;

    public CallStat(int line, Designator methodToCall, List<Expr> param) {
        super(line);
        this.methodToCall = new Call(line, methodToCall, param);
    }
    public CallStat(int line, Obj methodToCall) {
        super(line);
        this.methodToCall = new Call(line, methodToCall);
    }
    @Override
    public int toDOTString(StringBuilder sb, String parentName, int count) {
        return methodToCall.toDOTString(sb, parentName, count);
    }
    @Override
    public String getName() {
        return "Not used";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        methodToCall.execute(interpreter);
        if (methodToCall.type != Tab.noType) {
           interpreter.pop();
        }
    }
    @Override
    public TreeItem<Node> toTreeView() {
        return methodToCall.toTreeView();
    }
}
