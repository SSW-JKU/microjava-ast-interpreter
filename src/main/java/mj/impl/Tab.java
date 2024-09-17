package mj.impl;

import mj.run.Parser;
import mj.symtab.Scope;
import mj.symtab.Struct;

public final class Tab {

  // Universe
  public static final Struct noType = new Struct(Struct.Kind.None);
  public static final Struct intType = new Struct(Struct.Kind.Int);
  public static final Struct charType = new Struct(Struct.Kind.Char);
  public static final Struct nullType = new Struct(Struct.Kind.Class);

  public static final Obj noObj = new Obj(Obj.Kind.Var, "noObj", noType);
  public static final Obj chrObj = new Obj(Obj.Kind.Meth, "chr", charType);
  public static final Obj ordObj = new Obj(Obj.Kind.Meth, "ord", intType);
  public static final Obj lenObj = new Obj(Obj.Kind.Meth, "len", intType);
  public static final Obj intObj = new Obj(Obj.Kind.Type, "int", intType);
  public static final Obj charObj = new Obj(Obj.Kind.Type, "char", charType);
  public static final Obj nullObj = new Obj(Obj.Kind.Con, "null", nullType);
  /**
   * Only used for reporting errors.
   */
  private final Parser parser;
  /**
   * The current top scope.
   */
  public Scope curScope = null;
  // First scope opening (universe) will increase this to -1
  /**
   * Nesting level of current scope.
   */
  private int curLevel = -2;

  public Tab(Parser p) {
    parser = p;

    // opening scope (curLevel goes to -1, which is the universe level)
    openScope();


    curScope.insert(intObj);
    curScope.insert(charObj);
    curScope.insert(nullObj);

    //build chr()
    curScope.insert(chrObj);
    openScope();
    //insert "i" manual to set level = 1
    Obj i = new Obj(Obj.Kind.Var, "i", intType);
    i.level = 1;
    curScope.insert(i);
    chrObj.nPars = curScope.nVars();
    chrObj.locals = curScope.locals();
    closeScope();

    //build ord()
    curScope.insert(ordObj);
    openScope();
    //insert "ch" manual to set level = 1
    Obj ch = new Obj(Obj.Kind.Var, "ch", charType);
    ch.level = 1;
    curScope.insert(ch);
    ordObj.nPars = curScope.nVars();
    ordObj.locals = curScope.locals();
    closeScope();

    //build len();
    curScope.insert(lenObj);
    openScope();
    //insert "arr" manual to set level = 1
    Obj arr = new Obj(Obj.Kind.Var, "arr", new Struct(noType));
    arr.level = 1;
    curScope.insert(arr);
    lenObj.nPars = curScope.nVars();
    lenObj.locals = curScope.locals();
    closeScope();
  }

  public void openScope() {
    curScope = new Scope(curScope);
    curLevel++;
  }

  public void closeScope() {
    curScope = curScope.outer();
    curLevel--;
  }

  public Obj insert(Obj.Kind kind, String name, Struct type) {
    if (name == null || name.isEmpty()) {
      return noObj;
    }

    Obj newObj = new Obj(kind, name, type);

    newObj.level = curLevel;

    if (kind == Obj.Kind.Var) {
      newObj.adr = curScope.nVars();
    }

    if (curScope.findLocal(name) != null) {
      parser.SemErr("%s already declared".formatted(name));
    }

    curScope.insert(newObj);

    return newObj;
  }

  /**
   * Retrieves the object with <code>name</code> from the innermost scope.
   */
  public Obj find(String name) {
    Obj obj = curScope.findGlobal(name);
    if (obj == null) {
      obj = noObj;
      parser.SemErr("%s not found".formatted(name));
    }
    return obj;
  }

  /**
   * Retrieves the field <code>name</code> from the fields of
   * <code>type</code>.
   */
  public Obj findField(String name, Struct type) {
    Obj obj = type.findField(name);
    if (obj == null) {
      obj = noObj;
      parser.SemErr("%s is not a field".formatted(name));
    }
    return obj;
  }

  // ===============================================
  // ===============================================
}
