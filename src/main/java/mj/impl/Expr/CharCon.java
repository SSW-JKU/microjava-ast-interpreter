package mj.impl.Expr;

import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Tab;
import mj.run.Interpreter;

public class CharCon extends Expr {

    String value;
    char ch;

    public CharCon(int line, String value, char ch) {
        super(line, Tab.charType, 0, Kind.Con);
        this.value = value;
        this.ch = ch;
    }
    @Override
    public String getName() {
        return "CharCon (%s)".formatted(value);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        interpreter.push(ch);
    }
}
