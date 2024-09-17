package mj.impl;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.run.AbstractSyntaxTree;
import mj.run.Interpreter;

public abstract class Node {

    int line;
    boolean isBreakpoint;

    public Node(int line) {
        this.line = line;
        isBreakpoint = false;
    }
    public int buildDOTString(StringBuilder sb, String parentName, int count) {
        String name = "node%d".formatted(count);
        //link to parent
        sb.append("%s -> %s\n".formatted(parentName, name));
        //introduce node
        sb.append("%s [label = \"%s\", color = \"%s\"]\n".formatted(name, getName(), isBreakpoint? "red":"black"));
        return count;
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
    public TreeItem<Node> buildTreeView() {
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
