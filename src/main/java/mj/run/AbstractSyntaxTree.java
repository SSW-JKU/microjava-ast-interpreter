package mj.run;

import mj.impl.*;
import mj.impl.Exceptions.ControlFlowException;

import java.io.*;

public class AbstractSyntaxTree {

    public static final String TREE_FILENAME = "tree.dot";
    public static final String SVG_FILENAME = "tree.svg";
    public final String filename;
    private Interpreter interpreter;
    private final boolean compileError;

    public AbstractSyntaxTree(String filename) {
        this.filename = filename;

        System.out.println("----------------------------------");
        System.out.println("Abstract Syntax Tree for MicroJava");
        System.out.printf("   Reading source file %s%n", filename);
        Scanner scanner = new Scanner(filename);
        System.out.printf("   Parsing source file %s%n", filename);
        Parser parser = new Parser(scanner);

        Node root = parser.Parse();

        if (parser.errors.count == 1)
            System.out.println("-- 1 error detected");
        else
            System.out.printf("-- %d errors detected%n", parser.errors.count);

        this.compileError = parser.errors.count != 0;

        if (isCompiled()) {
            this.interpreter = new Interpreter(root);
            writeASTToFile(interpreter.getRoot());
        }
    }
    public void run() throws ControlFlowException {
        if (isCompiled()) {
            interpreter.run();
            if (interpreter.isDebug()) {
                interpreter.printData();
                interpreter.printHeap();
                interpreter.printMethodStack();
                interpreter.printExpressionStack();
            }
        }
    }
    public static void writeASTToFile(Node root) {

        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");
        root.buildDOTString(sb, "", 0);
        sb.append("}");

        try (FileWriter writer = new FileWriter(TREE_FILENAME)) {
            writer.write(sb.toString());
            ProcessBuilder processBuilder = new ProcessBuilder("dot", "-Tsvg", TREE_FILENAME, "-o", SVG_FILENAME);
            processBuilder.start();
        } catch (IOException e) {
            System.err.printf("An error occurred while writing to the file: %s%n", e.getMessage());
        }
    }
    public Node getRoot() {
        return interpreter.getRoot();
    }
    public boolean isCompiled() {
        return !compileError;
    }
    public void setDebug(boolean flag, Object lock) {
        interpreter.setDebugMode(flag);
        interpreter.setLock(lock);
    }
    public Interpreter getInterpreter() {
        return interpreter;
    }
}
