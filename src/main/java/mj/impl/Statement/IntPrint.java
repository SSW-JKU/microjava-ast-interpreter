package mj.impl.Statement;

import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Expr;
import mj.run.Interpreter;

public class IntPrint extends Print {
    public IntPrint(int line, Expr expr, int width) {
        super(line, expr, width);
    }
    public IntPrint(int line, Expr expr) {
        super(line, expr);
    }
    @Override
    public String getName() {
        return "IntPrint (width = %d)".formatted(width);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        System.out.print(val);
    }
}
