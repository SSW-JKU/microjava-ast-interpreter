package mj.impl.Expr;

import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Tab;
import mj.run.Interpreter;

public class IntCon extends Expr {
    private final String strVal;
    private final int val;
    public IntCon(int line, String strVal) {
        super(line, Tab.intType, 0, Kind.Con);
        this.strVal = strVal;
        this.val = Integer.parseInt(strVal);
    }
    public IntCon(int line, int val) {
        super(line, Tab.intType, 0, Kind.Con);
        this.strVal = String.valueOf(val);
        this.val = val;
    }
    @Override
    public String getName() {
        return "IntCon (%s)".formatted(strVal);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        interpreter.push(val);
    }
}
