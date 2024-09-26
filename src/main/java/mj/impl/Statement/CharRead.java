package mj.impl.Statement;

import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Designator;
import mj.impl.Expr.Expr;
import mj.run.Interpreter;

import java.util.Scanner;

public class CharRead extends Read {
    public CharRead(int line, Designator var) {
        super(line, var);
    }
    @Override
    public String getName() {
        return "CharRead";
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        Scanner scanner = new Scanner(System.in);
        char val = scanner.next().charAt(0);
        if (var.kind == Expr.Kind.Elem || var.kind == Expr.Kind.Fld) {
            var.execute(interpreter);
        }
        interpreter.assign(var, val);
    }
}
