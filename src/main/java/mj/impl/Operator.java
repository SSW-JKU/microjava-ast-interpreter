package mj.impl;

public enum Operator {
    NOOP("NOOP"), ADD("+"), SUB("-"), MUL("*"), DIV("/"), REM("%"), ASSIGN("="),
    PLUSAS("+="), MINUSAS("-="), TIMESAS("*="), SLASHAS("/="), REMAS("%="), EXP("**"),
    PPLUS("++"), MMINUS("--"),
    EQL("=="), NEQ("!="), LSS("<"), LEQ("<="), GTR(">"), GEQ(">="), AND("&&"),
    OR("||");

    private final String name;

    Operator(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
}
