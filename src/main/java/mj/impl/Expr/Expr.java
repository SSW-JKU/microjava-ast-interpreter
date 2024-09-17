package mj.impl.Expr;

import mj.impl.Node;
import mj.symtab.Struct;

public abstract class Expr extends Node {

    public enum Kind {
        Con, Local, Static, Fld, Elem, Meth, None
    }

    public Struct type;
    public int offset;

    public Kind kind;

    Expr(int line, Struct type, int offset, Kind kind) {
        super(line);
        this.type = type;
        this.offset = offset;
        this.kind = kind;
    }
}
