package mj.impl;

import mj.symtab.Struct;

import java.util.Collections;
import java.util.Map;

/**
 * MicroJava Symbol Table Objects: Every named object in a program is stored in
 * an <code>Obj</code> node. Every scope has a list of objects declared within
 * it.
 */
public class Obj {
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

  public Obj(Kind kind, String name, Struct type) {
    this.kind = kind;
    this.name = name;
    this.type = type;
  }
}