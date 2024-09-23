package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.impl.Operator;
import mj.impl.Tab;
import mj.run.Interpreter;

public class BinExpr extends Expr {

    Expr left;
    Expr right;
    Operator op;

    public BinExpr(int line, Expr left, Operator op, Expr right) {
        super(line, Tab.intType, 0, Kind.Con);
        this.left = left;
        this.op = op;
        this.right = right;
    }
    @Override
    public int toDOTString(StringBuilder sb, String parentName, int count) {
        super.toDOTString(sb, parentName, count);
        String name = "node%d".formatted(count);
        count = left.toDOTString(sb, name, count + 1);
        count = right.toDOTString(sb, name, count + 1);
        return count;
    }
    @Override
    public String getName() {
        return "BinExpr (%s)".formatted(op);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);

        left.execute(interpreter);
        int lval = interpreter.pop();
        boolean blval = (lval == 1);

        if (op == Operator.OR && blval) {
            interpreter.push(1);
            return;
        }
        else if (op == Operator.AND && !blval) {
            interpreter.push(0);
            return;
        }

        right.execute(interpreter);
        int rval = interpreter.pop();
        boolean brval = (rval == 1);

        switch (op) {
            case ADD:
                interpreter.push(lval + rval);
                break;
            case SUB:
                interpreter.push(lval - rval);
                break;
            case MUL:
                interpreter.push(lval * rval);
                break;
            case DIV:
                if (rval == 0) {
                    throw new IllegalStateException("division by zero");
                }
                interpreter.push(lval / rval);
                break;
            case REM:
                if (rval == 0) {
                    throw new IllegalStateException("division by zero");
                }
                interpreter.push(lval % rval);
                break;
            case EXP:
                int val = 1;
                for (int i = 0; i < rval; i++) {
                    val *= lval;
                }
                interpreter.push(val);
                break;
            case EQL:
                interpreter.push((lval == rval)? 1:0);
                break;
            case NEQ:
                interpreter.push((lval != rval)? 1:0);
                break;
            case LSS:
                interpreter.push((lval < rval)? 1:0);
                break;
            case LEQ:
                interpreter.push((lval <= rval)? 1:0);
                break;
            case GTR:
                interpreter.push((lval > rval)? 1:0);
                break;
            case GEQ:
                interpreter.push((lval >= rval)? 1:0);
                break;
            case AND, OR:
                interpreter.push(brval? 1:0);
                break;
            default:
                throw new IllegalStateException("Operator not implemented");
        }
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(left.toTreeView());
        item.getChildren().add(right.toTreeView());
        return item;
    }
}
