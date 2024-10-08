package mj.impl.Expr;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.run.Interpreter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Designator extends Expr {
    private final Ident ident;
    private final List<Expr> exprList;
    public Designator(int line, Ident ident) {
        super(line, ident.type, ident.offset, ident.kind);
        this.ident = ident;
        this.exprList = new ArrayList<>();
    }
    @Override
    public void toDOTString(StringBuilder sb, String parentName) {
        super.toDOTString(sb, parentName);
        ident.toDOTString(sb, dotId);
        for (Expr expr : exprList) {
            expr.toDOTString(sb, dotId);
        }
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
    public Ident getIdent() {
        return ident;
    }
}
