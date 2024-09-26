package mj.run;

import mj.impl.*;
import mj.impl.Statement.*;
import mj.impl.Expr.*;
import mj.symtab.Struct;
import mj.impl.Tab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;



public class Parser {
	public static final int _EOF = 0;
	public static final int _number = 1;
	public static final int _charConst = 2;
	public static final int _ident = 3;
	public static final int maxT = 47;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	private static final int MAX_GLOBALS = 32767;

  /**
   * Maximum number of fields per class
   */
  private static final int MAX_FIELDS = 32767;

  /**
   * Maximum number of local variables per method
   */
  private static final int MAX_LOCALS = 127;

  public final Tab tab = new Tab(this);

  private Obj curMethod = Tab.noObj;

  private final Map<Obj, Ident> methods = new HashMap<>();

  private char parseChar(String val) {

    char ch = ' ';

    if (val.charAt(1) != '\\') {
        ch = val.charAt(1);
    }
    else {
        switch (val.charAt(2)) {
            case 'r' : ch = '\r'; break;
            case 'n' : ch = '\n'; break;
            case '\'': ch = '\''; break;
            case '\\': ch = '\\'; break;
        }
    }
    return ch;
  }

  public Ident.Kind getIdentKind(Obj var) {

      Ident.Kind kind;

      switch (var.kind) {
          case Con:
              kind = Ident.Kind.Con;
              break;
          case Var:
              if (var.level == 0) {
                  kind = Ident.Kind.Static;
              }
              else {
                  kind = Ident.Kind.Local;
              }
              break;
          case Meth:
              kind = Ident.Kind.Meth;
              break;
          default:
              kind = Ident.Kind.None;
              SemErr("cannot create code operand for this kind of symbol table object");
      }
      return kind;
  }

  Expr load(Designator designator) {
        switch (designator.kind) {
            case Con:
            case Local:
            case Static:
            case Meth:
                //value already on stack -> nothing to load
                break;
            case Fld:
                designator.addExpr(new LoadFld(0, designator.offset));
                break;
            case Elem:
                designator.addExpr(new LoadElem(0));
                break;
            default:
                SemErr("value expected");
                break;
        }
        return null;
    }

  Expr loadExpr(Expr expr) {
        switch (expr.kind) {
            case Con:
            case Local:
            case Static:
            case Meth:
                //value already on stack -> nothing to load
                return expr;
            case Fld:
                return new LoadFld(0, expr, expr.offset);
            case Elem:
                return new LoadElem(0, expr);
            default:
                SemErr("value expected");
                return expr;
        }
    }



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	Node  ASTMircoJava() {
		Node  program;
		Expect(4);
		Expect(3);
		Obj prog = tab.insert(Obj.Kind.Prog, t.val, Tab.noType);
		program = new Ident(t.line, prog);
		Obj chr = tab.find("chr");
		methods.put(chr, new Ident(0, chr));
		Obj ord = tab.find("ord");
		methods.put(ord, new Ident(0, ord));
		Obj len = tab.find("len");
		methods.put(len, new Ident(0, len));
		tab.openScope();
		while (la.kind == 3 || la.kind == 7 || la.kind == 11) {
			if (la.kind == 7) {
				ConstDecl();
			} else if (la.kind == 3) {
				VarDecl();
			} else {
				ClassDecl();
			}
		}
		if (tab.curScope.nVars() > MAX_GLOBALS) { SemErr("too many global variables"); }
		Expect(5);
		while (la.kind == 3 || la.kind == 12) {
			MethodDecl();
		}
		Expect(6);
		Obj main = tab.curScope.findGlobal("main");
		if (main == null || main.kind != Obj.Kind.Meth) { SemErr("method main not found"); }
		((Ident)program).setMain(new CallStat(0, methods.get(main)));
		((Ident)program).setMethods(methods.values().stream().collect(Collectors.toList()));
		prog.locals = tab.curScope.locals();
		tab.closeScope();
		return program;
	}

	void ConstDecl() {
		Struct type;
		Expect(7);
		type = Type();
		Expect(3);
		Obj con = tab.insert(Obj.Kind.Con, t.val, type);
		Expect(8);
		if (la.kind == 1) {
			if (!type.equals(Tab.intType)) { SemErr("value does not match constant type"); }
			Get();
			con.val = Integer.parseInt(t.val);
		} else if (la.kind == 2) {
			if (!type.equals(Tab.charType)) { SemErr("value does not match constant type"); }
			Get();
			con.val = parseChar(t.val);
		} else SynErr(48);
		Expect(9);
	}

	void VarDecl() {
		Struct type;
		type = Type();
		Expect(3);
		tab.insert(Obj.Kind.Var, t.val, type);
		while (la.kind == 10) {
			Get();
			Expect(3);
			tab.insert(Obj.Kind.Var, t.val, type);
		}
		Expect(9);
	}

	void ClassDecl() {
		Expect(11);
		Expect(3);
		Obj clazz = tab.insert(Obj.Kind.Type, t.val, new Struct(Struct.Kind.Class));
		Expect(5);
		tab.openScope();
		while (la.kind == 3) {
			VarDecl();
			if (tab.curScope.nVars() > MAX_FIELDS) { SemErr("too many fields"); }
		}
		clazz.type.fields = tab.curScope.locals();
		tab.closeScope();
		Expect(6);
	}

	void MethodDecl() {
		Stat block;
		Struct type = Tab.noType;
		if (la.kind == 3) {
			type = Type();
			if (type.isRefType()) { SemErr("methods may only return int or char"); }
		} else if (la.kind == 12) {
			Get();
		} else SynErr(49);
		Expect(3);
		String methodName = t.val;
		curMethod = tab.insert(Obj.Kind.Meth, methodName, type);
		Ident method = new Ident(t.line, curMethod);
		methods.put(curMethod, method);
		tab.openScope();
		Expect(13);
		if (la.kind == 3) {
			FormPars();
		}
		Expect(14);
		curMethod.nPars = tab.curScope.nVars();
		if (methodName.equals("main")) {
		     if (!type.isEqual(Tab.noType)) { SemErr("main method must return void"); }
		     if (curMethod.nPars > 0) { SemErr("main method must not have any parameters"); }
		}
		while (la.kind == 3) {
			VarDecl();
		}
		if (tab.curScope.nVars() > MAX_LOCALS) { SemErr("too many local variables"); }
		curMethod.locals = tab.curScope.locals();
		block = Block(false);
		method.setBlock((Block)block);
		tab.closeScope();
	}

	Struct  Type() {
		Struct  type;
		Expect(3);
		Obj obj = tab.find(t.val);
		if (obj.kind != Obj.Kind.Type) { SemErr("type expected"); }
		type = obj.type;
		if (la.kind == 15) {
			Get();
			Expect(16);
			type = new Struct(type);
		}
		return type;
	}

	void FormPars() {
		Struct type;
		Obj param;
		type = Type();
		Expect(3);
		param = tab.insert(Obj.Kind.Var, t.val, type);
		while (la.kind == 10) {
			Get();
			type = Type();
			Expect(3);
			param = tab.insert(Obj.Kind.Var, t.val, type);
		}
	}

	Stat  Block(boolean inLoop) {
		Stat  node;
		Stat stat;
		Block block = new Block(0);
		Expect(5);
		block.setLine(t.line);
		while (StartOf(1)) {
			stat = Statement(inLoop);
			block.add(stat);
		}
		Expect(6);
		node = block;
		return node;
	}

	Stat  Statement(boolean inLoop) {
		Stat  node;
		node = null;
		Operator op;
		Expr expr;
		Expr cond;
		Designator var;
		Stat stat;
		List<Expr> param;
		int lineOfOp;
		int lineOfCall;
		int lineOfWhile;
		int lineOfBreak;
		int lineOfIf;
		int lineOfPrint;
		int lineOfRead;
		switch (la.kind) {
		case 3: {
			var = Designator();
			lineOfCall = t.line;
			if (StartOf(2)) {
				op = Assignop();
				lineOfOp = t.line;
				expr = Expr();
				if (!var.canBeAssignedTo()) { SemErr("cannot store to operand kind " + var.getIdent().getObj().name); }
				if (op == Operator.ASSIGN) {
				 if (!expr.type.assignableTo(var.type)) { SemErr("incompatible types"); }
				}
				else {
				 if (var.type != Tab.intType || expr.type != Tab.intType) { SemErr("operand(s) must be of type int"); }
				}
				switch (op) {
				 case ASSIGN:
				     node = new Assignment(lineOfOp, var, loadExpr(expr));
				     break;
				 case PLUSAS:
				 case MINUSAS:
				 case TIMESAS:
				 case SLASHAS:
				 case REMAS:
				     node = new BinOpAssignment(lineOfOp, var, op, loadExpr(expr));
				     break;
				}
			} else if (la.kind == 13) {
				param = ActPars(var);
				node = new CallStat(lineOfCall, methods.get(var.getIdent().getObj()), param);
			} else if (la.kind == 17) {
				Get();
				if (!var.canBeAssignedTo()) { SemErr("cannot store to operand kind " + var.kind); }
				if (var.type != Tab.intType) { SemErr("operand(s) must be of type int"); }
				node = new BinOpAssignment(t.line, var, Operator.PPLUS, new IntCon(t.line, 1));
			} else if (la.kind == 18) {
				Get();
				if (!var.canBeAssignedTo()) { SemErr("cannot store to operand kind " + var.kind); }
				if (var.type != Tab.intType) { SemErr("operand(s) must be of type int"); }
				node = new BinOpAssignment(t.line, var, Operator.MMINUS, new IntCon(t.line, 1));
			} else SynErr(50);
			Expect(9);
			break;
		}
		case 19: {
			Get();
			lineOfIf = t.line;
			Expect(13);
			cond = Condition();
			Expect(14);
			stat = Statement(inLoop);
			node = new If(lineOfIf, cond, stat);
			if (la.kind == 20) {
				Get();
				lineOfIf = t.line;
				stat = Statement(inLoop);
				node = new ElseIf(lineOfIf, (If)node, stat);
			}
			break;
		}
		case 21: {
			Get();
			lineOfWhile = t.line;
			Expect(13);
			cond = Condition();
			Expect(14);
			stat = Statement(true);
			node = new While(lineOfWhile, cond, stat);
			break;
		}
		case 22: {
			Get();
			lineOfBreak = t.line;
			Expect(9);
			if (!inLoop) { SemErr("break is not within a loop"); }
			node = new Break(lineOfBreak);
			break;
		}
		case 23: {
			Get();
			node = new Return(t.line);
			if (StartOf(3)) {
				expr = Expr();
				if (curMethod.type == Tab.noType) { SemErr("void method must not return a value"); }
				node = new Return(t.line, loadExpr(expr));
			}
			else if (curMethod.type != Tab.noType) { SemErr("return expression required"); }
			Expect(9);
			break;
		}
		case 24: {
			Get();
			lineOfRead = t.line;
			Expect(13);
			var = Designator();
			if (!var.canBeAssignedTo()) { SemErr("cannot store to operand kind " + var.kind); }
			if (var.type == Tab.intType) { node = new IntRead(lineOfRead, var); }
			else if (var.type == Tab.charType) { node = new CharRead(lineOfRead, var); }
			else { SemErr("can only read int or char values"); }
			Expect(14);
			Expect(9);
			break;
		}
		case 25: {
			int width = 0;
			Get();
			lineOfPrint = t.line;
			Expect(13);
			expr = Expr();
			if (expr.type != Tab.intType && expr.type != Tab.charType) { SemErr("can only print int or char values " + expr.type); }
			if (la.kind == 10) {
				Get();
				Expect(1);
				width = Integer.parseInt(t.val);
			}
			if (expr.type == Tab.intType) { node = new IntPrint(lineOfPrint, loadExpr(expr), width); }
			else { node = new CharPrint(lineOfPrint, loadExpr(expr), width); }
			Expect(14);
			Expect(9);
			break;
		}
		case 5: {
			node = Block(inLoop);
			break;
		}
		case 9: {
			Get();
			node = new Semicolon(t.line);
			break;
		}
		default: SynErr(51); break;
		}
		return node;
	}

	Designator  Designator() {
		Designator  node;
		Ident ident;
		Expr expr;
		Expr loadNode;
		Obj obj;
		Expect(3);
		obj = tab.find(t.val);
		node = new Designator(t.line, new Ident(t.line, obj, getIdentKind(obj)));
		while (la.kind == 15 || la.kind == 42) {
			if (la.kind == 42) {
				if (node.type.kind != Struct.Kind.Class) { SemErr("dereferenced object is not a class"); }
				Get();
				Expect(3);
				load(node);
				obj = tab.findField(t.val, node.type);
				node.kind = Expr.Kind.Fld;
				node.type = obj.type;
				node.offset = obj.adr;
			} else {
				Get();
				expr = Expr();
				if (node.type.kind != Struct.Kind.Arr) { SemErr("indexed object is not an array"); }
				if (expr.type != Tab.intType) { SemErr("array index must be an integer"); }
				load(node);
				node.addExpr(loadExpr(expr));
				node.kind = Expr.Kind.Elem;
				node.type = node.type.elemType;
				Expect(16);
			}
		}
		return node;
	}

	Operator  Assignop() {
		Operator  op;
		op = Operator.NOOP;
		switch (la.kind) {
		case 8: {
			Get();
			op = Operator.ASSIGN;
			break;
		}
		case 26: {
			Get();
			op = Operator.PLUSAS;
			break;
		}
		case 27: {
			Get();
			op = Operator.MINUSAS;
			break;
		}
		case 28: {
			Get();
			op = Operator.TIMESAS;
			break;
		}
		case 29: {
			Get();
			op = Operator.SLASHAS;
			break;
		}
		case 30: {
			Get();
			op = Operator.REMAS;
			break;
		}
		default: SynErr(52); break;
		}
		return op;
	}

	Expr  Expr() {
		Expr  expr;
		Operator op;
		Expr expr2;
		boolean sign = false;
		int lineOfSign = 0;
		int lineOfOp = 0;
		if (la.kind == 39) {
			Get();
			sign = true;
			lineOfSign = t.line;
		}
		expr = Term();
		if (sign) {
		   if (expr.type != Tab.intType) { SemErr("operand(s) must be of type int"); }
		   expr = new UnaryExpr(lineOfSign, Operator.SUB, loadExpr(expr));
		}
		while (la.kind == 39 || la.kind == 43) {
			op = Addop();
			lineOfOp = t.line;
			expr2 = Term();
			if (expr.type != Tab.intType || expr2.type != Tab.intType) { SemErr("operand(s) must be of type int"); }
			expr = new BinExpr(lineOfOp, loadExpr(expr), op, loadExpr(expr2));
		}
		return expr;
	}

	List<Expr>  ActPars(Designator designator) {
		List<Expr>  param;
		param = new ArrayList<>();
		Expr expr;
		Obj meth;
		Expect(13);
		if (designator.kind != Expr.Kind.Meth) { SemErr("called object is not a method"); }
		meth = designator.getIdent().getObj();
		int aPars = 0;
		int fPars = meth.nPars;
		Iterator<Obj> pars = meth.locals.values().iterator();
		Obj fp;
		if (StartOf(3)) {
			expr = Expr();
			param.add(loadExpr(expr));
			aPars++;
			if (pars.hasNext()) {
			 fp = pars.next();
			 if (!expr.type.assignableTo(fp.type)) { SemErr("parameter type mismatch"); }
			}
			while (la.kind == 10) {
				Get();
				expr = Expr();
				param.add(loadExpr(expr));
				aPars++;
				if (pars.hasNext()) {
				 fp = pars.next();
				 if (!expr.type.assignableTo(fp.type)) { SemErr("parameter type mismatch"); }
				}
			}
		}
		if (aPars > fPars) { SemErr("more actual than formal parameters"); }
		if (aPars < fPars) { SemErr("less actual than formal parameters"); }
		Expect(14);
		return param;
	}

	Expr  Condition() {
		Expr  cond;
		Expr cond2;
		int lineOfOp;
		cond = CondTerm();
		while (la.kind == 31) {
			Get();
			lineOfOp = t.line;
			cond2 = CondTerm();
			cond = new BinExpr(lineOfOp, loadExpr(cond), Operator.OR, loadExpr(cond2));
		}
		return cond;
	}

	Expr  CondTerm() {
		Expr  cond;
		Expr cond2;
		int lineOfOp;
		cond = CondFact();
		while (la.kind == 32) {
			Get();
			lineOfOp = t.line;
			cond2 = CondFact();
			cond = new BinExpr(lineOfOp, loadExpr(cond), Operator.AND, loadExpr(cond2));
		}
		return cond;
	}

	Expr  CondFact() {
		Expr  cond;
		Expr cond2;
		Operator op;
		int lineOfOp;
		cond = Expr();
		op = Relop();
		lineOfOp = t.line;
		cond2 = Expr();
		if (!cond.type.compatibleWith(cond2.type)) { SemErr("incompatible types"); }
		if ((cond.type.isRefType() || cond2.type.isRefType())
		     && op != Operator.EQL
		     && op != Operator.NEQ) { SemErr("only (un)equality checks are allowed for reference types"); }
		cond = new BinExpr(lineOfOp, loadExpr(cond), op, loadExpr(cond2));
		return cond;
	}

	Operator  Relop() {
		Operator  op;
		op = Operator.NOOP;
		switch (la.kind) {
		case 33: {
			Get();
			op = Operator.EQL;
			break;
		}
		case 34: {
			Get();
			op = Operator.NEQ;
			break;
		}
		case 35: {
			Get();
			op = Operator.GTR;
			break;
		}
		case 36: {
			Get();
			op = Operator.GEQ;
			break;
		}
		case 37: {
			Get();
			op = Operator.LSS;
			break;
		}
		case 38: {
			Get();
			op = Operator.LEQ;
			break;
		}
		default: SynErr(53); break;
		}
		return op;
	}

	Expr  Term() {
		Expr  expr;
		Operator op;
		Expr expr2;
		int lineOfOp;
		expr = Factor();
		while (StartOf(4)) {
			if (la.kind == 44 || la.kind == 45 || la.kind == 46) {
				op = Mulop();
				lineOfOp = t.line;
				expr2 = Factor();
				if (expr.type != Tab.intType || expr2.type != Tab.intType) { SemErr("operand(s) must be of type int"); }
				expr = new BinExpr(lineOfOp, loadExpr(expr), op, loadExpr(expr2));
			} else {
				Get();
				lineOfOp = t.line;
				Expect(1);
				expr = loadExpr(expr);
				if (expr.type != Tab.intType) { SemErr("operand(s) must be of type int"); }
				expr = new BinExpr(lineOfOp, expr, Operator.EXP, new IntCon(t.line, t.val));
			}
		}
		return expr;
	}

	Operator  Addop() {
		Operator  op;
		op = Operator.NOOP;
		if (la.kind == 43) {
			Get();
			op = Operator.ADD;
		} else if (la.kind == 39) {
			Get();
			op = Operator.SUB;
		} else SynErr(54);
		return op;
	}

	Expr  Factor() {
		Expr  expr;
		expr = new IntCon(t.line, 0);
		List<Expr> param;
		int lineOfCall;
		int lineOfNew;
		if (la.kind == 3) {
			expr = Designator();
			lineOfCall = t.line;
			if (la.kind == 13) {
				if (expr.type == Tab.noType) { SemErr("invalid call of void method"); }
				param = ActPars((Designator)expr);
				expr = new Call(lineOfCall, methods.get(((Designator)expr).getIdent().getObj()), param);
			}
		} else if (la.kind == 1) {
			Get();
			expr = new IntCon(t.line, t.val);
		} else if (la.kind == 2) {
			Get();
			expr = new CharCon(t.line, t.val, parseChar(t.val));
		} else if (la.kind == 41) {
			Get();
			lineOfNew = t.line;
			Expect(3);
			Obj obj = tab.find(t.val);
			Ident ident = new Ident(t.line, obj);
			Struct type = obj.type;
			if (obj.kind != Obj.Kind.Type) { SemErr("type expected"); }
			if (la.kind == 15) {
				Get();
				expr = Expr();
				if (expr.type != Tab.intType) { SemErr("array size must be an integer"); }
				expr = new NewArray(lineOfNew, ident, expr);
				Expect(16);
			}
			else {
			 if (type.kind != Struct.Kind.Class) { SemErr("class type expected"); }
			 expr = new New(lineOfNew, ident);
			}
		} else if (la.kind == 13) {
			Get();
			expr = Expr();
			Expect(14);
		} else SynErr(55);
		return expr;
	}

	Operator  Mulop() {
		Operator  op;
		op = Operator.NOOP;
		if (la.kind == 44) {
			Get();
			op = Operator.MUL;
		} else if (la.kind == 45) {
			Get();
			op = Operator.DIV;
		} else if (la.kind == 46) {
			Get();
			op = Operator.REM;
		} else SynErr(56);
		return op;
	}



	public Node Parse() {
		la = new Token();
		la.val = "";		
		Get();
Node root = 		ASTMircoJava();
		Expect(0);

		scanner.buffer.Close();
		return root;
	}

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x},
		{_x,_x,_x,_T, _x,_T,_x,_x, _x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_T, _x,_T,_T,_T, _T,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x},
		{_x,_x,_x,_x, _x,_x,_x,_x, _T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_T,_T, _T,_T,_T,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x},
		{_x,_T,_T,_T, _x,_x,_x,_x, _x,_x,_x,_x, _x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_T, _x,_T,_x,_x, _x,_x,_x,_x, _x},
		{_x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _T,_x,_x,_x, _T,_T,_T,_x, _x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "number expected"; break;
			case 2: s = "charConst expected"; break;
			case 3: s = "ident expected"; break;
			case 4: s = "\"program\" expected"; break;
			case 5: s = "\"{\" expected"; break;
			case 6: s = "\"}\" expected"; break;
			case 7: s = "\"final\" expected"; break;
			case 8: s = "\"=\" expected"; break;
			case 9: s = "\";\" expected"; break;
			case 10: s = "\",\" expected"; break;
			case 11: s = "\"class\" expected"; break;
			case 12: s = "\"void\" expected"; break;
			case 13: s = "\"(\" expected"; break;
			case 14: s = "\")\" expected"; break;
			case 15: s = "\"[\" expected"; break;
			case 16: s = "\"]\" expected"; break;
			case 17: s = "\"++\" expected"; break;
			case 18: s = "\"--\" expected"; break;
			case 19: s = "\"if\" expected"; break;
			case 20: s = "\"else\" expected"; break;
			case 21: s = "\"while\" expected"; break;
			case 22: s = "\"break\" expected"; break;
			case 23: s = "\"return\" expected"; break;
			case 24: s = "\"read\" expected"; break;
			case 25: s = "\"print\" expected"; break;
			case 26: s = "\"+=\" expected"; break;
			case 27: s = "\"-=\" expected"; break;
			case 28: s = "\"*=\" expected"; break;
			case 29: s = "\"/=\" expected"; break;
			case 30: s = "\"%=\" expected"; break;
			case 31: s = "\"||\" expected"; break;
			case 32: s = "\"&&\" expected"; break;
			case 33: s = "\"==\" expected"; break;
			case 34: s = "\"!=\" expected"; break;
			case 35: s = "\">\" expected"; break;
			case 36: s = "\">=\" expected"; break;
			case 37: s = "\"<\" expected"; break;
			case 38: s = "\"<=\" expected"; break;
			case 39: s = "\"-\" expected"; break;
			case 40: s = "\"**\" expected"; break;
			case 41: s = "\"new\" expected"; break;
			case 42: s = "\".\" expected"; break;
			case 43: s = "\"+\" expected"; break;
			case 44: s = "\"*\" expected"; break;
			case 45: s = "\"/\" expected"; break;
			case 46: s = "\"%\" expected"; break;
			case 47: s = "??? expected"; break;
			case 48: s = "invalid ConstDecl"; break;
			case 49: s = "invalid MethodDecl"; break;
			case 50: s = "invalid Statement"; break;
			case 51: s = "invalid Statement"; break;
			case 52: s = "invalid Assignop"; break;
			case 53: s = "invalid Relop"; break;
			case 54: s = "invalid Addop"; break;
			case 55: s = "invalid Factor"; break;
			case 56: s = "invalid Mulop"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
