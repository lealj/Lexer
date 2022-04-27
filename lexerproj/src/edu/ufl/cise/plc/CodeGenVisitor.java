package edu.ufl.cise.plc;

import java.util.List;

import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
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
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;
import edu.ufl.cise.plc.runtime.ConsoleIO;
import edu.ufl.cise.plc.runtime.FileURLIO;


@SuppressWarnings("exports")
public class CodeGenVisitor implements ASTVisitor {
	String packageName; 
	public CodeGenVisitor(String input) {
		packageName = input; 
	}
	
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		CodeGenStringBuilder sb = new CodeGenStringBuilder();
		
		String name = program.getName(); 
		String returnType = program.getReturnType().toString().toLowerCase(); 
		Type retType = program.getReturnType();
		/*
		//check parameters
		List<NameDef> params = program.getParams(); 
		for(NameDef nd : params)
		{
			nd.setInitialized(true);
			nd.visit(this, arg);
		}
		//Check declarations and statements
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}
		 */
		//package
		sb.append("package " + packageName).semi().newline();
		
		//imports
		if(retType == Type.IMAGE)
		{
			sb.append("import edu.ufl.cise.plc.runtime.*").semi().newline(); 
			sb.append("import java.awt.image.BufferedImage").semi().newline();
		}
		//class dec
		sb.append("public class " + name + "{").newline(); 
		//function dec
		if(retType != Type.STRING && retType != Type.IMAGE)
		{
			sb.append("public static " + returnType + " apply");
		}
		else if(retType == Type.IMAGE)
		{
			sb.append("public static BufferedImage apply");
		}
		else {
			sb.append("public static String apply");
		}
		sb.lparen();
		//params
		List<NameDef> params = program.getParams(); 
		for (int i =0; i < params.size(); i++)
		{
			NameDef nd = params.get(i);
			nd.visit(this, sb);
			if(i != params.size()-1)
			{
				sb.comma().space();
			}
		}
		
		sb.rparen();
		sb.append("{").newline();
		//decs and statements
		List<ASTNode> decsAndStmts = program.getDecsAndStatements();
		for (ASTNode nd : decsAndStmts)
		{
			nd.visit(this, sb);
		}
		sb.newline();
		sb.append("}");
		sb.newline().append("}");

		return sb.delegate.toString();
	}
	
	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		String type = "";
		//System.out.println("Name Def");
		if(nameDef.getType() != Type.STRING)
		{
			type = nameDef.getType().toString().toLowerCase(); 
		}
		else {
			type = "String";
		}
		String name = nameDef.getName(); 
		sb.append(type);
		sb.space(); 
		sb.append(name); 
		
		return sb;
	}
	
	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		String name = declaration.getName();
		NameDef nd = declaration.getNameDef();
		String expr = "";

		
		if(declaration.getExpr() != null)
		{
			expr = declaration.getExpr().getText();
		}
		
		nd.visit(this, arg);

		if(declaration.getOp() != null && nd.getType() != Type.IMAGE)
		{
			sb.equal();
			declaration.getExpr().visit(this, arg);
			sb.semi().newline();
		}
		else if(nd.getType() == Type.IMAGE)
		{
			Dimension dim = declaration.getDim(); 
			String width = dim.getWidth().getText();
			String height = dim.getHeight().getText(); 
			// no initial
			if(declaration.getExpr() == null)
			{
				// no initial, has dim
				sb.append("BufferedImage " + name + "= new BufferedImage("
						+ width + "," + height + "," + "BufferedImage.TYPE_INT_RGB);").newline();
				sb.append("for(int x=0; x<"
						+ name + ".getWidth();x++)").newline(); 
				sb.append("for(int y=0; y<"
						+ name + ".getHeight(); y++)").newline();
				
				
				sb.append("ImageOps.setColor(" + name + ",x,y,");
				// System.out.println(declaration.get + "DSFS");
			}
			// has initializser (expr)
			else {
				// has initial, has dim 
				if(dim != null)
				{
					sb.append("BufferedImage " + name + " = FileURLIO.readImage(url,width,height);").newline();
					sb.append("FileURLIO.closeFiles();");	
				}
				else {
					// no initial, no dim
					System.out.println("has initial, no dim");
				}
			}
		}
		else
		{
			sb.semi();
		}
		sb.newline();
		return sb;
	}
	
	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		
		conditionalExpr.getCondition().visit(this, arg);

		//String cond = conditionalExpr.getCondition().get
		
		String t = conditionalExpr.getTrueCase().getText(); 
		String f = conditionalExpr.getFalseCase().getText(); 
		
		sb.ques(); 
		conditionalExpr.getTrueCase().visit(this, arg); 
		sb.colon(); 
		conditionalExpr.getFalseCase().visit(this, arg); 
		
		return sb;
	}
	
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		
		sb.lparen(); 
		binaryExpr.getLeft().visit(this, arg); 
		String op = binaryExpr.getOp().getText(); 
		sb.append(op);
		binaryExpr.getRight().visit(this, arg); 
		sb.rparen(); 
		
		return sb;
	}
	
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		String val = booleanLitExpr.getText(); 
		
		sb.append(val);
		return sb;
	}
	
	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		Type coerceTo = consoleExpr.getCoerceTo();
		String coerceString = "";
		sb.lparen();
		if(coerceTo == Type.STRING)
		{
			coerceString = "String";
		}
		else {
			coerceString = coerceTo.toString().toLowerCase();
		}
		
		sb.append(coerceString);
		sb.rparen();
		
		if(ConsoleIO.console != null)
		{
			ConsoleIO.console.flush();
		}
		Object x = ConsoleIO.readValueFromConsole(coerceString.toUpperCase(), 
				"Enter " + coerceString + ": ");
		
		sb.append(x.toString());
		
		return sb;
	}
	
	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		String val = floatLitExpr.getText(); 
		
		if(floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != Type.FLOAT)
		{
			String castTo = floatLitExpr.getCoerceTo().toString().toLowerCase(); 
			sb.cast(castTo); 
		}

		sb.append(val).append("f"); 
		
		return sb;
	}
	
	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		String val = intLitExpr.getText(); 

		if(intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Type.FLOAT && 
				intLitExpr.getType() != Type.INT)
		{
			String castTo = intLitExpr.getCoerceTo().toString().toLowerCase(); 
			sb.cast(castTo); 
		}
		sb.append(val); 
		return sb;
	}
	
	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		String val = identExpr.getText(); 

		//System.out.println("Ident Expr");
		if(identExpr.getCoerceTo() != null && identExpr.getCoerceTo() != Type.FLOAT)
		{
			String castTo = identExpr.getCoerceTo().toString(); 
			sb.cast(castTo); 
		}
		sb.append(val); 
		return sb;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		String val = stringLitExpr.getValue(); 
		sb.tripleQuote().newline();
		sb.append(val); 
		sb.tripleQuote();
		return sb;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		String op = unaryExpression.getOp().getText(); 
		String expr = unaryExpression.getExpr().getText(); 
		
		sb.lparen(); 
		sb.append(op);
		sb.append(expr);
		sb.rparen(); 
		return sb;
	}
	
	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		String name = readStatement.getName(); 
	
		sb.append(name);
		sb.equal(); 
		readStatement.getSource().visit(this, arg);
		sb.semi();
		return sb;
	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		String expr = writeStatement.getSource().getText(); 
		ConsoleIO.console.println(expr); 
		return null;
	}
	
	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		String name = assignmentStatement.getName(); 
		String val = assignmentStatement.getExpr().getText();
		
		// check that ident type is not image. 
		if(assignmentStatement.getTargetDec().getType() != Type.IMAGE)
		{
			sb.append(name); 
			sb.equal();
			assignmentStatement.getExpr().visit(this, arg);
		
			sb.semi().newline(); 
		}
		else {
			assignmentStatement.getExpr().visit(this, arg);
		}
		return sb;
	}

	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		Expr expr = returnStatement.getExpr(); 
		
		sb.append("return").space(); 
		expr.visit(this, sb); 
		sb.semi();
		return sb;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		
		return null;
	}
	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		
		return null;
	}
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg; 
		Expr b = colorExpr.getBlue();
		
		String blue = colorExpr.getBlue().getText(); 
		String red = colorExpr.getRed().getText(); 
		String green = colorExpr.getGreen().getText(); 
		
		sb.append("new ColorTuple(" + red + "," + green + "," + blue + "));").newline();
		
		return null;
	}

}
	
	
	
/*
 * visitor pattern to traverse ast
 * create string containing java class representing PLCLang prog
 * can write generated code to a file or dynamically compile/execute generated code
 * dynamic compilation and execution allows us to embed plclang programs in java programs
 * 
 * each visit method accepts a stringbuilder or similar as paramter and appends the java
 * code corresponding to that construct.
 * it is convenient if each visit method returns the string builder that htey were passed. 
 */


