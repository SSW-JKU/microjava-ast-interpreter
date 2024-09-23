package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Exceptions.ReturnException;
import mj.impl.Exceptions.TrapException;
import mj.impl.Node;
import mj.impl.Obj;
import mj.impl.Tab;
import mj.run.Interpreter;

import java.util.ArrayList;
import java.util.List;

public class Call extends Expr {

    Obj methodToCall;
    List<Expr> paras;

    public Call(int line, Obj methodToCall) {
        super(line, methodToCall == null? Tab.noType : methodToCall.type, 0, Kind.Meth);
        this.methodToCall = methodToCall;
        this.paras = new ArrayList<>();
    }
    public Call(int line, Designator methodToCall, List<Expr> param) {
        super(line, methodToCall == null? Tab.noType : methodToCall.type, 0, Kind.Meth);
        this.methodToCall = methodToCall == null? null : methodToCall.getObj();
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
        return "Call %s".formatted(methodToCall.name);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);

        //load parameters
        for (Expr para : paras) {
            para.execute(interpreter);
        }

        if (methodToCall == Tab.ordObj || methodToCall == Tab.chrObj) {
            //nothing -> parameter is return value
        }
        else if (methodToCall == Tab.lenObj) {
            int adr = interpreter.pop();
            if (adr == 0) {
                throw new IllegalStateException("null reference used");
            }
            interpreter.push(interpreter.getHeap(adr - 1));
        }
        else {
            int psize = methodToCall.nPars;
            int lsize = methodToCall.locals.size();
            interpreter.allocMethodStack(psize, lsize);

            try {
                methodToCall.execute(interpreter);
            } catch (ReturnException ex) {
                //return case
                interpreter.freeMethodStack();
                return;
            }

            if (methodToCall.type != Tab.noType) {
                throw new TrapException("Method %s has no return statement".formatted(methodToCall.name));
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
