package mj.impl.Statement;

import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Designator;
import mj.run.Interpreter;

import java.util.Scanner;

public class CharRead extends Read {
    public CharRead(int line, Designator var) {
        super(line, var);
    }

    @Override
    public int toDOTString(StringBuilder sb, String parentName, int count) {
        super.toDOTString(sb, parentName, count);
        String name = "node%d".formatted(count);
        count = var.toDOTString(sb, name, count + 1);
        return count;
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
        interpreter.assign(var, val);
    }
}
