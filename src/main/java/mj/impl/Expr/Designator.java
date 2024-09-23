package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.impl.Obj;
import mj.run.Interpreter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Designator extends Expr {
    Ident ident;
    List<Expr> exprList;

    public Designator(int line, Ident ident) {
        super(line, ident.type, ident.offset, ident.kind);
        this.ident = ident;
        this.exprList = new ArrayList<>();
    }
    @Override
    public int toDOTString(StringBuilder sb, String parentName, int count) {
        super.toDOTString(sb, parentName, count);
        String name = "node%d".formatted(count);
        count = ident.toDOTString(sb, name, count + 1);
        for (Expr expr : exprList) {
            count = expr.toDOTString(sb, name, count + 1);
        }
        return count;
    }
    @Override
    public String getName() {
        return "Designator (Kind: %s)".formatted(kind);
    }
    @Override
    public void execute(Interpreter interpreter) throws ControlFlowException {
        super.execute(interpreter);
        ident.execute(interpreter);
        for (Expr expr : exprList) {
            expr.execute(interpreter);
        }
    }
    @Override
    public TreeItem<Node> toTreeView() {
        TreeItem<Node> item = super.toTreeView();
        item.getChildren().add(ident.toTreeView());
        for (Expr expr : exprList) {
            item.getChildren().add(expr.toTreeView());
        }
        return item;
    }
    public void addExpr(Expr expr) {
        if (expr == this) return;
        exprList.add(expr);
    }
    public boolean canBeAssignedTo() {
        return EnumSet.of(Kind.Local, Kind.Static, Kind.Fld, Kind.Elem).contains(kind);
    }
    public Obj getObj() {
        return ident.getVar();
    }
}
