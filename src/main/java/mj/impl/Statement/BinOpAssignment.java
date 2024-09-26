package mj.impl.Statement;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Designator;
import mj.impl.Expr.Expr;
import mj.impl.Node;
import mj.impl.Operator;
import mj.run.Interpreter;

public class BinOpAssignment extends Stat {
    private final Designator var;
    private final Operator op;
    private final Expr expr;
    public BinOpAssignment(int line, Designator var, Operator op, Expr expr) {
        super(line);
        this.var = var;
        this.op = op;
        this.expr = expr;
    }
    @Override
    public void toDOTString(StringBuilder sb, String parentName) {
        super.toDOTString(sb, parentName);
        var.toDOTString(sb, dotId);
        if (op != Operator.PPLUS && op != Operator.MMINUS) {
            expr.toDOTString(sb, dotId);
        }
    }
    @Override
    public String getName() {
        return "BinOpAssignment (%s)".formatted(op);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {

        int adr, idx, val;

        var.execute(interpreter);
        switch (var.kind) {
            case Local:
            case Static:
                break;
            case Fld:
                adr = interpreter.pop();
                interpreter.push(adr);
                interpreter.push(interpreter.getHeap(adr + var.offset));
                break;
            case Elem:
                idx = interpreter.pop();
                adr = interpreter.pop();
                interpreter.push(adr);
                interpreter.push(idx);
                interpreter.push(interpreter.getHeap(adr + idx));
                break;
        }

        expr.execute(interpreter);

        switch(op) {
            case PLUSAS:
            case PPLUS:
                val = interpreter.pop() + interpreter.pop();
                break;
            case MINUSAS:
            case MMINUS:
                val = -interpreter.pop() + interpreter.pop();
                break;
            case TIMESAS:
                val = interpreter.pop() * interpreter.pop();
                break;
            case SLASHAS:
                val = interpreter.pop();
                if (val == 0) {
                    throw new IllegalStateException("division by zero");
                }
                val = interpreter.pop() / val;
                break;
            case REMAS:
                val = interpreter.pop();
                if (val == 0) {
                    throw new IllegalStateException("division by zero");
                }
                val = interpreter.pop() % val;
                break;
            default:
                throw new IllegalStateException("Operator not implemented");
        }
        interpreter.assign(var, val);
    }

    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(var.toTreeView());
        if (op != Operator.PPLUS && op != Operator.MMINUS) {
            item.getChildren().add(expr.toTreeView());
        }
        return item;
    }
}
