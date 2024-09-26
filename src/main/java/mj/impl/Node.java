package mj.impl;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.run.AbstractSyntaxTree;
import mj.run.Interpreter;

public abstract class Node {
    int line;
    boolean isBreakpoint;
    protected final String dotId;
    private static int count = 0;


    public Node(int line) {
        this.line = line;
        this.dotId = "node%d".formatted(count);
        count++;
        isBreakpoint = false;
    }
    public void toDOTString(StringBuilder sb, String parentName) {
        //link to parent
        if (!parentName.equals("")) {
            sb.append("%s -> %s\n".formatted(parentName, dotId));
        }
        //introduce node
        sb.append("%s [label = \"%s\", color = \"%s\"]\n".formatted(dotId, getName(), isBreakpoint? "red":"black"));
    }
    public abstract String getName();
    public void execute(Interpreter interpreter) throws ControlFlowException {
        if (interpreter.isDebug()) {
            interpreter.setLineOfExecution(line);
            Object lock = interpreter.getLock();
            synchronized (lock) {
                try {
                    isBreakpoint = true;
                    AbstractSyntaxTree.writeASTToFile(interpreter.getRoot());
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    isBreakpoint = false;
                }
            }
        }
    }
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = new TreeItem<>(this);
        item.setExpanded(true);
        return item;
    }
    @Override
    public String toString() {
        return getName();
    }
    public int getLine() {
        return line;
    }
    public void setLine(int line) {
        this.line = line;
    }
    public boolean isCurrentBreakpoint() {
        return isBreakpoint;
    }

}
