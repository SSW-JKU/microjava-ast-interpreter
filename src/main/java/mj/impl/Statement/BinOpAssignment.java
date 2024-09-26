package mj.impl.Statement;

import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Designator;
import mj.impl.Expr.Expr;
import mj.impl.Operator;
import mj.run.Interpreter;

public class BinOpAssignment extends Stat {

    Designator var;
    Operator op;
    Expr expr;

    public BinOpAssignment(int line, Designator var, Operator op, Expr expr) {
        super(line);
        this.var = var;
        this.op = op;
        this.expr = expr;
    }

    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {

        int adr, idx;
        int val = 0;

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

    @Override
    public String getName() {
        return null;
    }
}
