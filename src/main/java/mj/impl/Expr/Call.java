package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Exceptions.ReturnException;
import mj.impl.Exceptions.TrapException;
import mj.impl.Node;
import mj.impl.Tab;
import mj.run.Interpreter;

import java.util.ArrayList;
import java.util.List;

public class Call extends Expr {

    private final Ident methodToCall;
    private final List<Expr> paras;

    public Call(int line, Ident methodToCall) {
        super(line, methodToCall == null? Tab.noType : methodToCall.type, 0, Kind.Meth);
        this.methodToCall = methodToCall;
        this.paras = new ArrayList<>();
    }
    public Call(int line, Ident methodToCall, List<Expr> param) {
        super(line, methodToCall == null? Tab.noType : methodToCall.type, 0, Kind.Meth);
        this.methodToCall = methodToCall;
        this.paras = param;
    }
    @Override
    public int toDOTString(StringBuilder sb, String parentName, int count) {
        super.toDOTString(sb, parentName, count);
        String name = "node%d".formatted(count);
        for (Expr para : paras) {
            count = para.toDOTString(sb, name, count + 1);
        }
        return count;
    }
    @Override
    public String getName() {
        return "Call %s".formatted(methodToCall.getObj().name);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);

        //load parameters
        for (Expr para : paras) {
            para.execute(interpreter);
        }

        if (methodToCall.getObj() == Tab.ordObj || methodToCall.getObj() == Tab.chrObj) {
            //nothing -> parameter is return value
        }
        else if (methodToCall.getObj() == Tab.lenObj) {
            int adr = interpreter.pop();
            if (adr == 0) {
                throw new IllegalStateException("null reference used");
            }
            interpreter.push(interpreter.getHeap(adr - 1));
        }
        else {
            int psize = methodToCall.getObj().nPars;
            int lsize = methodToCall.getObj().locals.size();
            interpreter.allocMethodStack(psize, lsize);

            try {
                methodToCall.execute(interpreter);
            } catch (ReturnException ex) {
                //return case
                interpreter.freeMethodStack();
                return;
            }

            if (methodToCall.type != Tab.noType) {
                throw new TrapException("Method %s has no return statement".formatted(methodToCall.getObj().name));
            }
            else {
                interpreter.freeMethodStack();
            }
        }
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        for (Expr para : paras) {
            item.getChildren().add(para.toTreeView());
        }
        return item;
    }
}
