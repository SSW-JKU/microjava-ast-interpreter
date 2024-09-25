package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.impl.Obj;
import mj.impl.Statement.Block;
import mj.impl.Statement.CallStat;
import mj.impl.Tab;
import mj.run.Interpreter;

import java.util.List;
import java.util.Map;

public class Ident extends Expr {

    private final Obj obj;
    private Node main;
    private  Block block;
    private List<Ident> methods;

    public Ident(int line, Obj obj, Kind kind) {
        super(line, obj==null? Tab.noType:obj.type, obj==null? 0:obj.adr, kind);
        this.obj = obj;
        this.main = null;
        this.block = null;
        this.methods = null;
    }
    public Ident(int line, Obj var) {
        this(line, var, Kind.None);
    }

    @Override
    public String getName() {
        switch (obj.kind) {
            case Prog:
                return "Program (%s)".formatted(obj.name);
            case Meth:
                return "Method (%s)".formatted(obj.name);
            case Var:
                switch (type.kind) {
                    case Int:
                    case Char:
                        return "Var (%s)".formatted(obj.name);
                    case Arr:
                        return "Var (%s [arr])".formatted(obj.name);
                    default:
                        //None
                }
            case Type:
                switch (type.kind) {
                    case Int:
                    case Char:
                        return "Type (%s)".formatted(obj.name);
                    case Class:
                        return "Type (%s [class])".formatted(obj.name);
                    case Arr:
                        return "Type (%s [arr])".formatted(obj.name);
                    default:
                        //None
                }
            case Con:
                return "Con (%d)".formatted(obj.val);
            default:
                return "not used";
        }
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);

        switch (obj.kind) {
            case Prog:
                main.execute(interpreter);
                break;
            case Meth:
                Ident caller = (Ident)interpreter.getCurMethod();
                interpreter.setCurMethod(this);
                block.execute(interpreter);
                interpreter.setCurMethod(caller);
                break;
            case Con:
                interpreter.push(obj.val);
                break;
            case Var:
                int adr;
                int idx;

                switch (kind) {
                    case Local:
                        interpreter.push(interpreter.getLocal(obj.adr));
                        break;
                    case Static:
                        interpreter.push(interpreter.getData(obj.adr));
                        break;
                    case Fld:
                        adr = interpreter.pop();
                        interpreter.push(interpreter.getHeap(adr + obj.adr));
                        break;
                    case Elem:
                        adr = interpreter.pop();
                        idx = interpreter.pop();
                        interpreter.push(interpreter.getHeap(adr+1+idx));
                        break;
                    default:
                        break;
                }
                break;
            default:
                throw new IllegalArgumentException("Kind not implemented");
        }
    }
    @Override
    public void toDOTString(StringBuilder sb, String parentName) {
        super.toDOTString(sb, parentName);

        switch (obj.kind) {
            case Prog:
                for (Ident method : methods) {
                    if (method.block != null) {
                        method.toDOTString(sb, dotId);
                    }
                }
                break;
            case Meth:
                if (block != null) {
                    sb.append("subgraph cluster_%s {\n".formatted(dotId));
                    block.toDOTString(sb, dotId);
                    sb.append("}\n");
                }
                break;
        }
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        switch (obj.kind) {
            case Prog:
                for (Ident method : methods) {
                    if (method.block != null) {
                        item.getChildren().add(method.toTreeView());
                    }
                }
                break;
            case Meth:
                if (block != null) {
                    item.getChildren().add(block.toTreeView());
                }
                break;
        }
        return item;
    }
    public void setMain(CallStat main) {
        this.main = main;
    }

    public int getDataSize() {
        int size = 0;
        for (Map.Entry<String, Obj> entry : obj.locals.entrySet()) {
            Obj obj = entry.getValue();
            if (obj.kind == Obj.Kind.Var) {
                size++;
            }
        }
        return size;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public Obj getObj() {
        return obj;
    }
    public void setMethods(List<Ident> methods) {
        this.methods = methods;
    }
}
