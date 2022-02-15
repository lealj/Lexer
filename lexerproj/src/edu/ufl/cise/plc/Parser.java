package edu.ufl.cise.plc;

import java.util.List;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.IntLitExpr;

public class Parser implements IParser {
	private int current = 0; 
	private List<Token> tokens; 
	
	public Parser(String input) {
		LexerClass lexer = new LexerClass(input); 
		this.tokens = lexer.tokens; 
	}
	
	private boolean match(Kind[] kinds)
	{
		for(int i = 0; i < kinds.length; i++)
		{
			if(check(kinds[i])) {
				advance(); 
				return true; 
			}
		}
		return false; 
	}
	
	private boolean check(Kind kind) {
		if(peek().getKind() == Kind.EOF) {
			return false; 
		}
		return peek().getKind() == kind; 
	}
	
	private IToken advance() {
		if(peek().getKind() != Kind.EOF)
		{
			current++; 
		}
		return tokens.get(current-1); 
	}


	private IToken peek() {
		return tokens.get(current);
	}

	private void error() {
		// TODO Auto-generated method stub
		
	}

	private void consume() {
		advance(); 
	}
	
	private Expr expression() {
		return equality(); 
	}
	
	Expr equality() {
		Expr expr = comparison(); 
		Kind[] kinds = {Kind.NOT_EQUALS, Kind.EQUALS); 
		while(match(kinds)) {
			Token op = tokens.get(current-1); 
			Expr right = comparison(); 
			
			expr = new BinaryExpr(expr, op, right); 
		}
		return expr; 
	}
	private Expr comparison() {
		Expr expr = term(); 
		Kind[] kinds = {Kind.GT, Kind.GE, Kind.LT, Kind.LE}; 
		while(match(kinds))
		{
			Token op = tokens.get(current-1); 
			Expr right = term(); 
			expr = new BinaryExpr(expr, op, right); 
		}
		return expr;
	}

	//next 3 functions from slides
	Expr factor() {
		IToken firstToken = peek(); 
		Expr e = null; 
		if(firstToken.getKind() == Kind.INT_LIT) {
			e = new IntLitExpr(firstToken); 
			consume(); 
		}
		else if(firstToken.getKind() == (Kind.LPAREN))
		{
			consume(); 
			e = expr(); 
			Kind[] kinds = {Kind.RPAREN}; 
			match(kinds); 
		}
		else {
			error(); 
		}
		return e; 
	}

	@SuppressWarnings("exports")
	public Expr expr() {
		IToken firstToken = peek(); 
		Expr left = null; 
		Expr right = null; 
		left = term(); 
		while(firstToken.getKind() == Kind.PLUS || firstToken.getKind() == Kind.MINUS)
		{
			IToken op = t; 
			consume(); 
			right = term(); 
			left = new BinaryExpr(firstToken, left, op, right); 
		}
		return left; 
	}
	//from book
	private Expr term() {
		Expr expr = factor(); 
		Kind[] kinds = {Kind.MINUS, Kind.PLUS}; 
		while(match(kinds))
		{
			IToken op = tokens.get(current-1); 
			Expr right = factor(); 
			expr = new BinaryExpr(expr, op, right); 
		}
		return expr; 
	}

	@SuppressWarnings("exports")
	@Override
	public ASTNode parse() throws PLCException {
		// TODO Auto-generated method stub
		return null;
	}
}
