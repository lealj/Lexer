package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.AssignmentStatement;
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
import edu.ufl.cise.plc.ast.Statement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;
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
			while((peek().getKind() == Kind.RETURN) || (peek().getKind() == Kind.TYPE || peek().getKind() == Kind.IDENT 
					|| peek().getKind() == Kind.KW_WRITE) && (next().getKind() == Kind.IDENT 
					|| next().getKind() == Kind.LSQUARE || next().getKind() == Kind.LARROW 
					|| next().getKind() == Kind.ASSIGN))
			{
				if(peek().getKind() == Kind.TYPE) {
					VarDeclaration varDec = varDec(); 
					decsAndStmts.add(varDec);
				}
				if(peek().getKind() == Kind.RETURN || peek().getKind() == Kind.IDENT 
						|| peek().getKind()  == Kind.KW_WRITE)
				{
					Statement stmt = stmt();
					decsAndStmts.add(stmt); 

					if(match(Kind.SEMI)) {}
					else
					{ 
						if(match(Kind.RPAREN)) {}
						if(match(Kind.SEMI)) {}
						else {
							error("no semi after stmt"); 
						}
					}
				}
			}
			
			if(peek().getKind() != Kind.EOF)
			{
				Expr e = expr(); 
				if(e.getType() == null)
				{
					error("error: statement wasn't picked up"); 
				}
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
		else {
			throw new SyntaxException("not valid type"); 
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

				if(peek().getKind() == Kind.SEMI || peek().getKind()==Kind.RPAREN)
				{
					consume(peek().getKind(), "expect ; or rparen");
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
	
	private Statement stmt() throws SyntaxException {
		IToken first = peek(); 
		Expr expr = null; 
		//Assignment/read
		if(match(Kind.IDENT))
		{
			PixelSelector pix = null; 
			if(match(Kind.LSQUARE))
			{
				//pixel selector
				Expr x = expr(); 

				consume(Kind.COMMA, "coma"); 
				Expr y = expr(); 

				consume(Kind.RSQUARE, "rsquare"); 

				pix = new PixelSelector(first, x, y); 
			}
	
			if(match(Kind.ASSIGN))
			{
				String name = first.getText(); 
				expr = expr(); 
				return new AssignmentStatement(first, name, pix, expr); 
			}
			else if(match(Kind.LARROW))
			{
				String name = first.getText(); 
				expr = expr(); 
				return new ReadStatement(first, name, pix, expr); 
			}
		}
		else if(match(Kind.KW_WRITE))
		{
			expr = expr(); 
			if(match(Kind.RARROW))
			{
				Expr exprRight = expr(); 
				return new WriteStatement(first, expr, exprRight); 
			}
		}
		else if(match(Kind.RETURN))
		{
			//we have if here
			expr = expr();
			//setting type here does nothing....
			return new ReturnStatement(first, expr); 
		}
		return null; 
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
		left = or(); 

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
		
		Expr condition = or(); 
		consume(Kind.RPAREN, "rparen2342424"); 

		Expr trueCase = expr();
		Expr falseCase = null; 
		if(match(Kind.KW_ELSE))
		{
			falseCase = expr(); 
			match(Kind.KW_FI); 
		}
		ConditionalExpr ce = new ConditionalExpr(trueCase.getFirstToken(), 
				condition, trueCase, falseCase);
		ce.setType(trueCase.getType());
		
		return ce;
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
			expr.setType(Type.BOOLEAN);
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
			expr.setType(Type.INT);
			right.setType(Type.INT);
			
			expr = new BinaryExpr(firstToken, expr, op, right); 
			expr.setType(Type.BOOLEAN);
		}
		
		return expr;
	}

	private Expr term() throws SyntaxException {
		Expr e = factor(); 
		IToken firstToken = tokens.get(current-1); 
		Kind[] kinds = {Kind.MINUS, Kind.PLUS}; 
		while(match(kinds))
		{
			IToken op = tokens.get(current-1);
			Expr right = factor(); 
			e.setType(Type.INT);
			right.setType(Type.INT);
			e = new BinaryExpr(firstToken, e, op, right); 
		}
		return e; 
	}
	
	Expr factor() throws SyntaxException {
		IToken first = peek(); 
		Expr e = unary(); 

		Kind[] kinds = {Kind.DIV, Kind.TIMES, Kind.MOD};
		while(match(kinds)) {
			IToken op = tokens.get(current-1); 
			Expr right = unary(); 
			
			e = new BinaryExpr(first, e, op, right);
			if(peek().getKind() == Kind.RPAREN)
			{
				consume(peek().getKind(), "rparen in factor"); 
			}
		}

		return e; 
	}
	
	private Expr unary() throws SyntaxException {
		IToken first = peek(); 
		//////////////////////////////////////////////
		Kind[] kinds = {Kind.BANG, Kind.MINUS, Kind.COLOR_OP}; 
		if(match(kinds)) {
			IToken op = tokens.get(current-1); 
			Expr right = unary(); 
			right.setType(Type.INT);
			right.setCoerceTo(Type.COLOR);
			UnaryExpr ue = new UnaryExpr(first, op, right);
			return ue; 
		}

		if(next().getKind() == Kind.LSQUARE) { 
			Expr e = primary();
			consume(Kind.LSQUARE, "LSQUARE EXPECT"); 
			
			Expr x = term(); 
			x.setType(Type.INT);
			consume(Kind.COMMA, "COMMA EXPECT3434"); 
			
			Expr y = term(); 
			y.setType(Type.INT);
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
		if(match(Kind.COMMA))
		{
			
		}
		if(match(Kind.BOOLEAN_LIT)) {
			BooleanLitExpr ble = new BooleanLitExpr(first);
			ble.setType(Type.BOOLEAN);
			return ble;
		}
		if(match(Kind.INT_LIT))
		{
			IntLitExpr ile = new IntLitExpr(first);
			ile.setType(Type.INT);
			return ile; 
		}
		if(match(Kind.STRING_LIT)) {
			StringLitExpr sle = new StringLitExpr(first);
			sle.setType(Type.STRING);
			return sle;
		}
		if(match(Kind.FLOAT_LIT)) {
			FloatLitExpr fle = new FloatLitExpr(first);
			fle.setType(Type.FLOAT);
			return fle; 
		}
		if(match(Kind.IDENT)) {
			IdentExpr ie = new IdentExpr(first);
			
			if(peek().getKind() == Kind.LSQUARE  )
			{
				ie.setType(Type.IMAGE);
			}
			return ie; 
		}
		if(match(Kind.COLOR_CONST))
		{
			ColorConstExpr cce = new ColorConstExpr(first);
			cce.setType(Type.COLOR);
			return cce; 
		}

		if(match(Kind.LANGLE))
		{
			Expr r = expr(); 
			r.setType(Type.INT);
			match(Kind.COMMA); 
			
			Expr g = expr(); 
			g.setType(Type.INT);
			match(Kind.COMMA); 

			Expr b = expr(); 
			b.setType(Type.INT);

			match(Kind.RPAREN); 
			consume(Kind.RANGLE, "rangle");
		
			ColorExpr ce = new ColorExpr(first, r, g, b);
			ce.setType(Type.COLOR);
			return ce;
		}
		if(match(Kind.KW_CONSOLE))
		{
			return new ConsoleExpr(first); 
		}
		if(match(Kind.LPAREN)) 
		{
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
