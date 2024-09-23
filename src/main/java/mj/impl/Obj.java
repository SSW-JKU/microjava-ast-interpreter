package mj.impl;

import javafx.scene.control.TreeItem;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Statement.Block;
import mj.impl.Statement.CallStat;
import mj.run.Interpreter;
import mj.symtab.Struct;

import java.util.Collections;
import java.util.Map;

/**
 * MicroJava Symbol Table Objects: Every named object in a program is stored in
 * an <code>Obj</code> node. Every scope has a list of objects declared within
 * it.
 */
public class Obj extends Node {
  /**
   * Possible codes for object kinds.
   */
  public enum Kind {
    Con, Var, Type, Meth, Prog
  }

  /**
   * Kind of the object node.
   */
  public final Kind kind;

  /**
   * Name of the object node.
   */
  public final String name;

  /**
   * Type of the object node.
   */
  public final Struct type;

  /**
   * Only for Con: Value of the constant.
   */
  public int val;

  /**
   * Only for Var, Meth: Offset of the element.
   */
  public int adr;

  /**
   * Only for Var: Declaration level (0...global, 1...local)
   */
  public int level;


  /**
   * Only for Meth: Number of parameters.
   */
  public int nPars;

  // This is a Collections.emptyMap() on purpose, do not change this line
  // If you finished reading the locals of a method, use meth.locals = curScope.locals() and close the scope afterward
  /**
   * Only for Meth / Prog: List of local variables / global declarations.
   */
  public Map<String, Obj> locals = Collections.emptyMap();

  private Node mainNode;
  public Block block;

  public Obj(int line, Kind kind, String name, Struct type) {
    super(line);
    this.kind = kind;
    this.name = name;
    this.type = type;
    this.mainNode = null;
    this.block = new Block(0);
  }
  public Obj(Kind kind, String name, Struct type) {
    this(0, kind, name, type);
  }
  @Override
  public int toDOTString(StringBuilder sb, String parentName, int count) {
    String name = "node%d".formatted(count);

    switch (kind) {
      case Prog:
        //introduce node
        sb.append("%s [label = \"%s\"]\n".formatted(name, getName()));
        for (Obj obj : locals.values()) {
            count = obj.toDOTString(sb, name, count + 1);
        }
        break;
      case Meth:
        //because methods are unique, their names are used:
        name = this.name;

        //link to parent
        sb.append("%s -> %s\n".formatted(parentName, name));
        //introduce node
        sb.append("%s [label = \"%s\"]\n".formatted(name, getName()));
        sb.append("subgraph cluster_%s {\n".formatted(name));
        for (Obj obj : locals.values()) {
          count = obj.toDOTString(sb, name, count + 1);
        }
        count = block.toDOTString(sb, name, count + 1);
        sb.append("}\n");
        break;
      case Var:
      case Con:
        //link to parent
        sb.append("%s -> %s\n".formatted(parentName, name));
        //introduce node
        sb.append("%s [label = \"%s\"]\n".formatted(name, getName()));
        break;
      case Type:
        //link to parent
        sb.append("%s -> %s\n".formatted(parentName, name));
        //introduce node
        sb.append("%s [label = \"%s\"]\n".formatted(name, getName()));
        if (type.kind == Struct.Kind.Class) {
          for (Obj obj : type.fields.values()) {
            count = obj.toDOTString(sb, name, count + 1);
          }
        }
        break;
      default:
        break;
    }
    return count;
  }
  @Override
  public String getName() {
    switch (kind) {
      case Prog:
        return "Program (%s)".formatted(this.name);
      case Meth:
        return "Method (%s)".formatted(this.name);
      case Var:
        switch (type.kind) {
          case Int:
          case Char:
            return "Var (%s)".formatted(this.name);
          case Arr:
            return "Var (%s [arr])".formatted(this.name);
          default:
            //None
        }
      case Type:
        switch (type.kind) {
          case Int:
          case Char:
            return "Type (%s)".formatted(this.name);
          case Class:
            return "Type (%s [class])".formatted(this.name);
          case Arr:
            return "Type (%s [arr])".formatted(this.name);
          default:
            //None
        }
      case Con:
        return "Con (%d)".formatted(this.val);
      default:
        return "not used";
    }
  }
  @Override
  public void execute(Interpreter interpreter) throws ControlFlowException {
    switch (kind) {
      case Prog:
        mainNode.execute(interpreter);
        break;
      case Meth:
        Obj caller = interpreter.getCurMethod();
        interpreter.setCurMethod(this);
        block.execute(interpreter);
        interpreter.setCurMethod(caller);
        break;
      default:
        break;
    }
  }
  @Override
  public TreeItem<Node> toTreeView() {
     TreeItem<Node> item = super.toTreeView();
     switch (kind) {
      case Prog:
        for (Obj obj : locals.values()) {
          item.getChildren().add(obj.toTreeView());
        }
        break;
      case Meth:
        for (Obj obj : locals.values()) {
          item.getChildren().add(obj.toTreeView());
        }
        item.getChildren().add(block.toTreeView());
        break;
       case Type:
         if (type.kind == Struct.Kind.Class) {
           for (Obj obj : type.fields.values()) {
            item.getChildren().add(obj.toTreeView());
           }
         }
      default:
        break;
    }
    return item;
  }
  public void setMain(CallStat mainNode) {
    this.mainNode = mainNode;
  }

  public int getDataSize() {
    int size = 0;
    for (Map.Entry<String, Obj> entry : locals.entrySet()) {
      Obj obj = entry.getValue();
      if (obj.kind == Kind.Var) {
        size++;
      }
    }
    return size;
  }

}