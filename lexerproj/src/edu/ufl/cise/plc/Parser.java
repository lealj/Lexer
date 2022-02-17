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
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;

public class Parser implements IParser {
	private int current = 0; 
	private List<Token> tokens; 
	private LexerClass lexer; 
	//construct
	public Parser(String input) throws LexicalException {
		lexer = new LexerClass(input); 
		this.tokens = lexer.tokens; 
		tokenError(); 
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

	private IToken next() {
		return tokens.get(current+1); 
	}
	private IToken peek() {
		return tokens.get(current);
	}

	private void error(String msg) throws SyntaxException {
		throw new SyntaxException(msg); 
	}
	//receives the kind that is expected. throws error if no match. 
	private void consume(Kind kind, String msg) throws SyntaxException {
		if(peek().getKind() == kind)
		{
			advance();
		}
		else {
			error(msg); 
		}
	}
	
	//main functions
	@SuppressWarnings("exports")
	public Expr expr() throws SyntaxException {
		IToken firstToken = peek(); 
		Expr left = null; 
		Expr right = null; 
		left = term(); 
		Kind[] kinds = {Kind.PLUS, Kind.MINUS}; 
		while(match(kinds))
		{
			IToken op = tokens.get(current-1); 
			//consume(); 
			right = term(); 
			left = new BinaryExpr(firstToken, left, op, right); 
		}
		return left; 
	}
	
	//*****
		Expr equality() throws SyntaxException {
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
	
	private Expr comparison() throws SyntaxException {
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

	private Expr term() throws SyntaxException {
		Expr e = factor(); 
		IToken  firstToken = peek(); 
		Kind[] kinds = {Kind.MINUS, Kind.PLUS}; 
		while(match(kinds))
		{
			IToken op = tokens.get(current-1);
			Expr right = factor(); 
			e = new BinaryExpr(firstToken, e, op, right); 
		}
		return e; 
	}
	
	Expr factor() throws SyntaxException {
		IToken first = peek(); 
		Expr e = unary(); 
		Kind[] kinds = {Kind.DIV, Kind.TIMES};
		while(match(kinds)) {
			IToken op = tokens.get(current-1); 
			Expr right = factor(); 
			e = new BinaryExpr(first, e, op, right);
		}
		return e; 
	}
	
	private Expr unary() throws SyntaxException {
		IToken first = peek(); 
		Kind[] kinds = {Kind.BANG, Kind.MINUS}; 
		if(match(kinds)) {
			IToken op = tokens.get(current-1); 
			Expr right = unary(); 
			return new UnaryExpr(first, op, right); 
		}
		if(next().getKind() == Kind.LSQUARE) { 
			Expr e = primary();
			consume(Kind.LSQUARE, "LSQUARE EXPECT"); 
			Expr x = primary(); 
			consume(Kind.COMMA, "COMMA EXPECT"); 
			Expr y = primary(); 
			if(match(Kind.RSQUARE))
			{
				PixelSelector pix = new PixelSelector(first,x, y);
				return new UnaryExprPostfix(first, e, pix); 
			}
			else {
				//error -no right bracket
			}
			
		}
	
		return primary(); 
	}
	
	private Expr primary() throws SyntaxException {
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
			//consume(); 
			Expr expr = expr(); 
			//consume(); 
			return expr; 
		}
		error("unexpected token"); 
		return null; 
	}
	
	public void tokenError() throws LexicalException{
		for(int i = 0; i < tokens.size(); i++)
		{
			if(tokens.get(i).getKind() == Kind.ERROR) {
				throw new LexicalException("lex exception"); 
			}
		}
	}

	//passing tests 0,1,2,3,4,5,6,7,8,11,12,14 //called term() for these passes
	@SuppressWarnings("exports")
	@Override
	public ASTNode parse() throws PLCException {
		return expr();
	}
}
