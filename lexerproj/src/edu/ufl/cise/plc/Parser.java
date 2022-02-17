package edu.ufl.cise.plc;

import java.util.List;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.UnaryExpr;

public class Parser implements IParser {
	private int current = 0; 
	private List<Token> tokens; 
	//construct
	public Parser(String input) {
		LexerClass lexer = new LexerClass(input); 
		this.tokens = lexer.tokens; 
	}
	//helper functions
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
	
	private boolean match(Kind kind)
	{
		
		if(check(kind)) {
			advance(); 
			return true; 
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
	
	//main functions
	@SuppressWarnings("exports")
	public Expr expr() {
		IToken firstToken = peek(); 
		Expr left = null; 
		Expr right = null; 
		left = term(); 
		while(firstToken.getKind() == Kind.PLUS || firstToken.getKind() == Kind.MINUS)
		{
			IToken op = tokens.get(current-1); 
			consume(); 
			right = term(); 
			left = new BinaryExpr(firstToken, left, op, right); 
		}
		return left; 
	}
	
	//*****
		Expr equality() {
			Expr expr = comparison(); 
			IToken firstToken = peek(); 
			Kind[] kinds = {Kind.NOT_EQUALS, Kind.EQUALS}; 
			while(match(kinds)) {
				Token op = tokens.get(current-1); 
				Expr right = comparison(); 
				
				expr = new BinaryExpr(firstToken, expr, op, right); 
			}
			return expr; 
		}
	
	private Expr comparison() {
		Expr expr = term(); 
		IToken firstToken = peek(); 
		Kind[] kinds = {Kind.GT, Kind.GE, Kind.LT, Kind.LE}; 
		while(match(kinds))
		{
			Token op = tokens.get(current-1); 
			Expr right = term(); 
			expr = new BinaryExpr(firstToken, expr, op, right); 
		}
		return expr;
	}

	private Expr term() {
		Expr expr = factor(); 
		IToken  firstToken = peek(); 
		Kind[] kinds = {Kind.MINUS, Kind.PLUS}; 
		while(match(kinds))
		{
			IToken op = tokens.get(current-1); 
			Expr right = factor(); 
			expr = new BinaryExpr(firstToken, expr, op, right); 
		}
		return expr; 
	}
	
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
	
	private Expr unary() {
		IToken firstToken = peek(); 
		Kind[] kinds = {Kind.BANG, Kind.MINUS}; 
		if(match(kinds)) {
			IToken op = tokens.get(current-1); 
			Expr right = unary(); 
			return new UnaryExpr(firstToken, op, right); 
		}
		return primary(); 
	}
	
	private Expr primary() {
		IToken first = peek(); 
		if(match(Kind.BOOLEAN_LIT)) {
			return new BooleanLitExpr(first); 
		}
		if(match(Kind.INT_LIT))
		{
			return new IntLitExpr(first); 
		}
		if(match(Kind.STRING_LIT)) {
			return new StringLitExpr(first); 
		}
		if(match(Kind.FLOAT_LIT)) {
			return new FloatLitExpr(first); 
		}
		if(match(Kind.IDENT)) {
			return new IdentExpr(first); 
		}
		if(match(Kind.LPAREN)) {
			consume(); 
			Expr expr = expr(); 
			consume(); 
			return expr; 
		}
		return null; 
	}

	//passing tests 0,1,2,3,4,5
	@SuppressWarnings("exports")
	@Override
	public ASTNode parse() throws PLCException {
		return unary();
	}
}
