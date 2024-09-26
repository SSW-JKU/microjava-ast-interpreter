package mj.impl.Statement;

import mj.impl.Exceptions.BreakException;
import mj.impl.Exceptions.ControlFlowException;
import mj.run.Interpreter;

public class Break extends Stat {
    public Break(int line) {
        super(line);
    }
    @Override
    public String getName() {
        return "Break";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        throw new BreakException();
    }
}
