package mj.impl.Statement;

import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Designator;
import mj.run.Interpreter;

import java.util.Scanner;

public class IntRead extends Read {
    public IntRead(int line, Designator var) {
        super(line, var);
    }
    @Override
    public String getName() {
        return "IntRead";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        Scanner scanner = new Scanner(System.in);
        int val = scanner.nextInt();
        interpreter.assign(var, val);
    }
}
