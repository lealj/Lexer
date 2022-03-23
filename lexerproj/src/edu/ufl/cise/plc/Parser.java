package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.ColorConstExpr;

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
	
	/* Currently passing 0-6*/
	////////////////////////////////////////////////////////////////////////////////
	private Program prog() throws SyntaxException {
		IToken first = peek(); 
		Type type = null; 
		String name = null; 
		List<NameDef> params = new ArrayList<>(); 
		List<ASTNode> decsAndStmts = new ArrayList<>(); 
		if(match(Kind.TYPE) || match(Kind.KW_VOID))
		{
			type = Type.toType(tokens.get(current-1).getText()); 
			
			if(peek().getKind() == Kind.IDENT)
			{
				name = peek().getStringValue(); 
			}
			consume(Kind.IDENT, "prog ident"); 
			
			if(match(Kind.LPAREN))
			{
				//checks for ()
				if(peek().getKind() != Kind.RPAREN) {
					while(peek().getKind() != Kind.RPAREN || peek().getKind() != Kind.EOF)
					{
						NameDef nameDef = nameDef(); 
						if(peek().getKind() == Kind.COMMA || peek().getKind() == Kind.RPAREN)
						{
							params.add(nameDef); 
							if(peek().getKind() == Kind.COMMA)
							{
								consume(Kind.COMMA, "comma"); 
							}
							else if(peek().getKind() == Kind.RPAREN)
							{
								consume(Kind.RPAREN, "r paren"); 
								break; 
							}
						}
					}
				}
				else {
					consume(Kind.RPAREN, "r paren2"); 
				}
			}
			//declarations and statements
			while(peek().getKind() == Kind.TYPE && (next().getKind() == Kind.IDENT || next().getKind()==Kind.LSQUARE))
			{
				VarDeclaration varDec = varDec(); 
				decsAndStmts.add(varDec);
			}
		}
		return new Program(first, type, name, params, decsAndStmts);
	}
	/////
	private NameDef nameDef() throws SyntaxException {
		IToken first = peek(); 
		String type = null; 
		String name = null; 
		boolean image_op = false; 
		//match int, boolean, float, etc. 
		if(match(Kind.TYPE)) {
			type = tokens.get(current-1).getStringValue(); 
			//in case of image operation [1,2]
			Dimension dim = null; 
			if(match(Kind.LSQUARE))
			{
				image_op = true; 
				dim = dim(); 
			}
			name = peek().getStringValue(); 
			if(peek().getKind() == Kind.IDENT)
			{
				consume(Kind.IDENT, "ident loop"); 
			}
			if(image_op)
			{
				return new NameDefWithDim(first, type, name, dim);
			}

		}
		return new NameDef(first, type, name); 
	}
	
	private VarDeclaration varDec() throws SyntaxException {
		IToken first = peek(); 
		IToken op =null;
		Expr expr=null;
		NameDef nd=null; 
		if(peek().getKind()==Kind.TYPE) {
			nd = nameDef();
			if(match(Kind.ASSIGN)||match(Kind.LARROW))
			{
				op = tokens.get(current-1); 
				expr = expr(); 
				if(peek().getKind() == Kind.SEMI)
				{
					consume(Kind.SEMI, "expect ;");
				}
			}
		}
		if(peek().getKind() == Kind.SEMI)
		{
			consume(Kind.SEMI, "expect ;");
		}
		//set op/expr null if... ? 
		return new VarDeclaration(first, nd, op, expr); 
	}
	
	private Dimension dim() throws SyntaxException {
		IToken first = peek(); 
		Expr width = expr(); 
		consume(Kind.COMMA, "comma expect"); 
		Expr height = expr(); 
		consume(Kind.RSQUARE, "r - square"); 
		return new Dimension(first, width, height);
	}
	//////////////////////////////////////////////////////////////////////
	//main functions
	@SuppressWarnings("exports")
	public Expr expr() throws SyntaxException {
		IToken firstToken = peek(); 
		if(match(Kind.KW_IF)) {
			return ifState(); 
		}
		if(match(Kind.AND) || match(Kind.OR))
		{
			return or(); 
		}
		
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
	
	private Expr ifState() throws SyntaxException{
		IToken firstToken = peek(); 
		consume(Kind.LPAREN, "lparen"); 
		Expr condition = expr(); 
		consume(Kind.RPAREN, "rparen"); 
		Expr trueCase = expr();
		Expr falseCase = null; 
		if(match(Kind.KW_ELSE))
		{
			falseCase = expr(); 
		}
		
		return new ConditionalExpr(firstToken, condition, trueCase, falseCase);
	}
	
	private Expr or() throws SyntaxException{
		IToken firstToken = peek(); 
		Expr expr = and(); 
		while(match(Kind.OR))
		{
			IToken op = tokens.get(current-1); 
			Expr right = and(); 
			expr = new BinaryExpr(firstToken, expr, op, right); 
		}
		return expr; 
	}
	
	private Expr and() throws SyntaxException{
		IToken firstToken = peek(); 
		Expr expr = equality(); 
		while(match(Kind.AND))
		{
			
			IToken op = tokens.get(current-1); 
			Expr right = equality(); 
			expr = new BinaryExpr(firstToken, expr, op, right); 
			
		}
		return expr; 
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
		Kind[] kinds = {Kind.BANG, Kind.MINUS, Kind.COLOR_OP}; 
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
		if(match(Kind.COLOR_CONST))
		{
			return new ColorConstExpr(first); 
		}
		if(match(Kind.LANGLE))
		{
			Expr r = expr(); 
			consume(Kind.COMMA, "comaColor");
			Expr g = expr(); 
			consume(Kind.COMMA, "comaColor");
			Expr b = expr(); 
			consume(Kind.RANGLE, "rangle");
			return new ColorExpr(first, r, g, b); 
		}
		if(match(Kind.KW_CONSOLE))
		{
			return new ConsoleExpr(first); 
		}
		if(match(Kind.LPAREN)) {
			return expr(); 
		}
		if(match(Kind.KW_ELSE)) {
			return expr();
		}
		if(match(Kind.KW_FI)) {
			return expr();
		}
		if(match(Kind.SEMI))
		{
			return null; 
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
		return prog();
	}
}
