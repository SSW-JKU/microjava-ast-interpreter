package mj.impl.Exceptions;

public abstract class ControlFlowException extends Exception {
    public ControlFlowException() {
    }
    public ControlFlowException(String msg) {
        super(msg);
    }
}
