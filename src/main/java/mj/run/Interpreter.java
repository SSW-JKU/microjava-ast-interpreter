package mj.run;

import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Expr.Designator;
import mj.impl.Node;
import mj.impl.Obj;

public class Interpreter {

    private final int[] data; // global data
    private final int[] heap; // dynamic heap
    private final int[] stack; // expression stack
    private final int[] local; // locals
    private int fp, sp; // frame pointer, stack pointer on method stack on method stack
    private int esp; // expression stack pointer
    private int free; // next free heap address
    private static final int heapSize = 100000, // size of the heap in words
            mStackSize = 4000, // size of the method stack in words
            eStackSize = 30; // size of expression stack in words
    Node program;
    boolean debug;
    Object lock;
    Obj curMethod;
    int lineOfExecution;

    public Interpreter(Node program) {
        this.program = program;
        this.data = new int[((Obj)program).getDataSize()];
        this.heap = new int[heapSize];
        this.stack = new int[eStackSize];
        this.local = new int[mStackSize];
        this.fp = 0;
        this.sp = 0;
        this.esp = 0;
        this.free = 1;
        this.debug = false;
        this.curMethod = null;
        this.lineOfExecution = -1;
    }
    public void run() throws ControlFlowException {
        System.out.println("----------------------------------");
        System.out.println("           AST Console            ");
        System.out.println("----------------------------------");
        try {
            program.execute(this);
        } catch (ControlFlowException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("\n----------------------------------\n");
    }
    public void assign(Designator var, int val) throws ControlFlowException {

        int adr;
        int idx;

        switch (var.kind) {
            case Local:
                setLocal(var.offset, val);
                break;
            case Static:
                setData(var.offset, val);
                break;
            case Fld:
                var.execute(this);
                adr = pop();
                if (adr == 0) {
                    throw new IllegalStateException("null reference used");
                }
                setHeap(adr + var.offset, val);
                break;
            case Elem:
                var.execute(this);
                idx = pop();
                adr = pop();
                if (adr == 0) {
                    throw new IllegalStateException("null reference used");
                }
                int len = getHeap(adr - 1);
                if (idx < 0 || idx >= len) {
                    throw new IllegalStateException("index out of bounds");
                }
                setHeap(adr + idx, val);
                break;
            default:
                throw new IllegalStateException("value expected");
        }
    }
    public void push(int value) throws IllegalStateException {
        if (esp == eStackSize) {
            throw new IllegalStateException("expression stack overflow");
        }
        stack[esp] = value;
        esp++;
    }
    public int pop() throws IllegalStateException {
        if (esp == 0) {
            throw new IllegalStateException("expression stack underflow");
        }
        esp--;
        return stack[esp];
    }
    public void PUSH(int value) throws IllegalStateException {
        if (sp == mStackSize) {
            throw new IllegalStateException("method stack overflow");
        }
        local[sp] = value;
        sp++;
    }
    public int POP() throws IllegalStateException {
        if (sp == 0) {
            throw new IllegalStateException("method stack underflow");
        }
        sp--;
        return local[sp];
    }
    public int alloc(int size) {
        int adr = free;

        free += size;

        return adr;
    }
    public void allocMethodStack(int psize, int lsize) {
        PUSH(fp);
        fp = sp;

        for (int i = 0; i < lsize; i++) {
            PUSH(0);
        }
        assert sp == (fp + lsize);
        for (int i = psize - 1; i >= 0; i--) {
            local[fp + i] = pop();
        }
    }
    public void freeMethodStack() {
        sp = fp;
        fp = POP();
    }
    public void setData(int adr, int value) {
        data[adr] = value;
    }
    public void setHeap(int adr, int value) {
        heap[adr] = value;
    }
    public void setLocal(int adr, int value) {
        local[fp + adr] = value;
    }
    public int getData(int adr) {
        return data[adr];
    }
    public int getHeap(int adr) {
        return heap[adr];
    }
    public int getLocal(int adr) {
        return  local[fp + adr];
    }
    public void printHeap() {
        System.out.println("Heap data:");
        System.out.printf("Adr %4d:\t%10s%n", 0, "-");
        for (int i = 1; i < free; i++) {
            System.out.printf("Adr %4d:\t%10d%n", i, heap[i]);
        }
    }
    public void printMethodStack() {
        System.out.println("Method stack:");
        for (int i = 0; i < sp; i++) {
            System.out.printf("Adr %4d:\t%10d%n", i, local[i]);
        }
    }
    public void printData() {
        System.out.println("Global data:");
        for (int i = 0; i < data.length; i++) {
            System.out.printf("Adr %4d:\t%10d%n", i, data[i]);
        }
    }
    public void printExpressionStack() {
        System.out.println("Expression stack:");
        for (int i = 0; i < esp; i++) {
            System.out.printf("Adr %4d:\t%10d%n", i, stack[i]);
        }
    }
    public Node getRoot() {
        return program;
    }
    public void setDebugMode(boolean flag) {
        this.debug = flag;
    }
    public boolean isDebug() {
        return this.debug;
    }
    public void setLock(Object lock) {
        this.lock = lock;
    }
    public Object getLock() {
        return this.lock;
    }
    public Obj getCurMethod() {
        return curMethod;
    }
    public void setCurMethod(Obj callee) {
        curMethod = callee;
    }
    public int getLineOfExecution() {
        return lineOfExecution;
    }
    public void setLineOfExecution(int line) {
        lineOfExecution = line;
    }
}
