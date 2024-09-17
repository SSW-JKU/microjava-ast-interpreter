package mj.impl.Expr;

import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Obj;
import mj.impl.Tab;
import mj.run.Interpreter;

public class Ident extends Expr {

    Obj var;

    public Ident(int line, Obj var, Kind kind) {
        super(line, var==null? Tab.noType:var.type, var==null? 0:var.adr, kind);
        this.var = var;
    }
    @Override
    public String getName() {
        return "Ident (%s [%s])".formatted(var.name, kind);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);

        int adr;
        int idx;

        switch (kind) {
            case Con:
                interpreter.push(var.val);
                break;
            case Local:
                interpreter.push(interpreter.getLocal(var.adr));
                break;
            case Static:
                interpreter.push(interpreter.getData(var.adr));
                break;
            case Fld:
                adr = interpreter.pop();
                interpreter.push(interpreter.getHeap(adr + var.adr));
                break;
            case Elem:
                adr = interpreter.pop();
                idx = interpreter.pop();
                interpreter.push(interpreter.getHeap(adr+1+idx));
                break;
            default:
                break;
        }
    }
    public Obj getVar() {
        return var;
    }
}
