package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;

public class Parser implements IParser {
	public Parser(String input) {
		
	}
	abstract class Expr{
		public final IToken firstToken; 
		
		public Expr(IToken firstToken) {
			this.firstToken = firstToken; 
		}
		//...
	}
	class IntLitExpr extends Expr{
		public IntLitExpr(IToken firstToken) {
			super(firstToken); 
		}
		public int getValue(){
			return firstToken.getIntValue(); 
		}
		//...
	}
	class BinaryExpr extends Expr{
		public final Expr left; 
		public final IToken op; 
		public final Expr right; 
		public BinaryExpr(IToken firstToken, Expr left, IToken op, Expr right) {
			super(firstToken); 
			this.left = left; 
			this.op = op; 
			this.right = right; 
		}
		//...
	}
	//not sure if this is right spot
	Expr factor() {
		IToken firstToken = t; 
		Expr e = null; 
		if(isKind(INT_LIT)) {
			e = new IntLitExpr(firstToken); 
			consume(); 
		}
		else if(isKind(LPAREN))
		{
			consume(); 
			e = expr(); 
			match(RPAREN); 
		}
		else {
			error(); 
		}
		return e; 
	}
	
	private void error() {
		// TODO Auto-generated method stub
		
	}

	private void consume() {
		// TODO Auto-generated method stub
		
	}

	public Expr expr() {
		IToken firstToken = t; 
		Expre left = null; 
		Expr right = null; 
		left = term(); 
		while(isKind(PLUS) || isKind(MINUS))
		{
			IToken op = t; 
			consume(); 
			right = term(); 
			left = new BinaryExpr(firstToken, left, op, right); 
		}
		return left; 
	}

	private Expre term() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("exports")
	@Override
	public ASTNode parse() throws PLCException {
		// TODO Auto-generated method stub
		return null;
	}
}
