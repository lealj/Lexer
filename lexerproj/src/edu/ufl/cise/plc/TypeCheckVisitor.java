package edu.ufl.cise.plc;

import java.util.List;
import java.util.Map;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
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

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class TypeCheckVisitor implements ASTVisitor {

	SymbolTable symbolTable = new SymbolTable();  
	Program root;
	
	record Pair<T0,T1>(T0 t0, T1 t1){};  //may be useful for constructing lookup tables.
	
	private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
		if (!condition) {
			throw new TypeCheckException(message, node.getSourceLoc());
		}
	}
	
	//The type of a BooleanLitExpr is always BOOLEAN.  
	//Set the type in AST Node for later passes (code generation)
	//Return the type for convenience in this visitor.  
	@SuppressWarnings("exports")
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		booleanLitExpr.setType(Type.BOOLEAN);
		return Type.BOOLEAN;
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		stringLitExpr.setType(Type.STRING);
		return Type.STRING; 
	}
	
	@SuppressWarnings("exports")
	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		intLitExpr.setType(Type.INT);
		return Type.INT; 
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		floatLitExpr.setType(Type.FLOAT);
		return Type.FLOAT;
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		colorConstExpr.setType(Type.COLOR);
		return Type.COLOR; 
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		consoleExpr.setType(Type.CONSOLE);
		return Type.CONSOLE;
	}
	
	//Visits the child expressions to get their type (and ensure they are correctly typed)
	//then checks the given conditions.
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		Type redType = (Type) colorExpr.getRed().visit(this, arg);
		Type greenType = (Type) colorExpr.getGreen().visit(this, arg);
		Type blueType = (Type) colorExpr.getBlue().visit(this, arg);
		check(redType == greenType && redType == blueType, colorExpr, "color components must have same type");
		check(redType == Type.INT || redType == Type.FLOAT, colorExpr, "color component type must be int or float");
		Type exprType = (redType == Type.INT) ? Type.COLOR : Type.COLORFLOAT;
		colorExpr.setType(exprType);
		return exprType;
	}	
	
	//Maps forms a lookup table that maps an operator expression pair into result type.  
	//This more convenient than a long chain of if-else statements. 
	//Given combinations are legal; if the operator expression pair is not in the map, it is an error. 
	Map<Pair<Kind,Type>, Type> unaryExprs = Map.of(
			new Pair<Kind,Type>(Kind.BANG,BOOLEAN), BOOLEAN,
			new Pair<Kind,Type>(Kind.MINUS, FLOAT), FLOAT,
			new Pair<Kind,Type>(Kind.MINUS, INT),INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,INT), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,COLOR), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,IMAGE), IMAGE,
			new Pair<Kind,Type>(Kind.IMAGE_OP,IMAGE), INT
			);
	
	//Visits the child expression to get the type, then uses the above table to determine the result type
	//and check that this node represents a legal combination of operator and expression type. 
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
		// !, -, getRed, getGreen, getBlue
		Kind op = unaryExpr.getOp().getKind();
		Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
		//Use the lookup table above to both check for a legal combination of operator and expression, and to get result type.
		Type resultType = unaryExprs.get(new Pair<Kind,Type>(op,exprType));
		check(resultType != null, unaryExpr, "incompatible types for unaryExpr");
		//Save the type of the unary expression in the AST node for use in code generation later. 
		unaryExpr.setType(resultType);
		//return the type for convenience in this visitor.
		return resultType;
	}

	//This method has several cases. Work incrementally and test as you go. 
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		Kind op = binaryExpr.getOp().getKind();

		Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
		Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
		Type resultType = null;
		switch(op) {//AND, OR, PLUS, MINUS, TIMES, DIV, MOD, EQUALS, NOT_EQUALS, LT, LE, GT,GE 
			case EQUALS,NOT_EQUALS -> {
				check(leftType == rightType, binaryExpr, "incompatible types for comparison");
				resultType = Type.BOOLEAN;
			}
			case PLUS -> {
				if(leftType == rightType) resultType = leftType; 
				else check(false, binaryExpr, "incompatible types for operator1");
			}
			case  MINUS -> {
				if(leftType == rightType) resultType = leftType; 
				else check(false, binaryExpr, "incompatible types for operator2");
			}
			case TIMES -> {
				if(leftType == rightType) resultType = leftType; 
				else check(false, binaryExpr, "incompatible types for operator3");
			}
			case DIV -> {
				if(leftType == rightType) resultType = leftType; 
				else check(false, binaryExpr, "incompatible types for operator4");
			}
			case LT, LE, GT, GE -> {
				if(leftType == rightType) resultType = leftType; 
				else check(false, binaryExpr, "incompatible types for operator5");
			}
			default -> {
				throw new Exception("compiler error");
			}
		}
		binaryExpr.setType(resultType);
		return resultType;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		String name = identExpr.getText();
		Declaration dec = symbolTable.lookup(name); 
		//check if defined
		check(dec != null, identExpr, "undef ident " + name); 
		
		//check if initialized
		check(dec.isInitialized(), identExpr, "using uninitialized var " + name); 
		
		identExpr.setDec(dec);
		Type type = dec.getType();
		identExpr.setType(type);
		return type; 
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		String name = conditionalExpr.getCondition().getText(); 
		Type decType = symbolTable.lookup(name).getType();
		conditionalExpr.getCondition().setType(decType);
		Type condType = conditionalExpr.getCondition().getType(); 
		check(condType == Type.BOOLEAN, conditionalExpr, "condition not boolean"); 
		
		Type trueCaseType = conditionalExpr.getTrueCase().getType(); 
		Type falseCaseType = conditionalExpr.getFalseCase().getType(); 
		check(trueCaseType == falseCaseType, conditionalExpr, "true type != false type"); 
		return trueCaseType; 
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		Type x = dimension.getHeight().getType(); 
		Type y = dimension.getWidth().getType(); 
		check(x != Type.INT, dimension, "x != int"); 
		check(y != Type.INT, dimension, "y != int"); 
		return x; 
	}

	@SuppressWarnings("exports")
	@Override
	//This method can only be used to check PixelSelector objects on the right hand side of an assignment. 
	//Either modify to pass in context info and add code to handle both cases, or when on left side
	//of assignment, check fields from parent assignment statement.
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type xType = (Type) pixelSelector.getX().visit(this, arg);
		check(xType == Type.INT, pixelSelector.getX(), "only ints as pixel selector components");
		Type yType = (Type) pixelSelector.getY().visit(this, arg);
		check(yType == Type.INT, pixelSelector.getY(), "only ints as pixel selector components");
		return null;
	}
	
	@SuppressWarnings("exports")
	@Override
	//This method several cases--you don't have to implement them all at once.
	//Work incrementally and systematically, testing as you go.  
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		String name = assignmentStatement.getName(); 
		Declaration dec = symbolTable.lookup(name); 
		Type targType = dec.getType(); 
		//not image
		if(targType != Type.IMAGE)
		{
			if(dec != null)
			{
				dec.setInitialized(true);
			}
			
			Type exprType = assignmentStatement.getExpr().getType();
			//could be != null, am slepe deprived. shouldn't have selector on lhs
			check(assignmentStatement.getSelector() == null, assignmentStatement, "has selector"); 
			check(assignCompatible(targType, exprType), assignmentStatement, "incompatible"
					+ " !image"); 
		}
		//we have an image
		else {
			//there is no pixel selector
			Type exprType = assignmentStatement.getExpr().getType();
			if(assignmentStatement.getSelector() == null)
			{
				check(assignCompatible(targType, exprType), assignmentStatement, "incompatible"
						+ " (image -no selector"); 
				if(exprType == Type.INT) 
				{
					assignmentStatement.getExpr().setCoerceTo(COLOR); 
				}
				else if(exprType == Type.FLOAT)
				{
					assignmentStatement.getExpr().setCoerceTo(COLORFLOAT);
				}
			}
			//there is a pixel selector
			else 
			{
				assignmentStatement.getExpr().setCoerceTo(COLOR);
				//turned 4 failures into errors
				dec.setInitialized(true); 
				//Type xType = assignmentStatement.getSelector().getX().getType();
				assignmentStatement.getSelector().getX().setType(INT); 
				assignmentStatement.getSelector().getY().setType(INT); 
	
				String pix_name_x = assignmentStatement.getSelector().getX().getText(); 
				String pix_name_y = assignmentStatement.getSelector().getX().getText(); 

				Declaration dec_x = symbolTable.lookup(pix_name_x); 
				Declaration dec_y = symbolTable.lookup(pix_name_y); 
				
				boolean inserted = symbolTable.insert(pix_name_x, dec_x);
				
				//this is likely reffering to [x,y] not a
				check(inserted, dec_x, "lhs" + pix_name_x + "already declared");
				
				
				Type rhs = assignmentStatement.getExpr().getType();

				//check that rhs is color, colorfloat, float, int, and coerced to color 
				
				switch(rhs)
				{
					case COLOR, COLORFLOAT, FLOAT, INT->{
						assignmentStatement.getExpr().setCoerceTo(COLOR); 
					}
					default->{
						throw new TypeCheckException(
							"image w/ selector", 
							assignmentStatement.getFirstToken().getSourceLocation()) ;
						}
				}
			}
				
		}
		return null; 
	}

	private boolean assignCompatible(Type targType, Type exprType) {
		if(targType == exprType && targType != IMAGE)
		{
			return true;
		}
		switch(targType){
			case INT->{
				switch(exprType) {
					case FLOAT, COLOR->{return true;}
					default->{return false;}
				}
			}
			case FLOAT->{
				switch(exprType) {
					case INT->{return true;}
					default->{return false;}
				}
			}
			case COLOR->{
				switch(exprType) {
					case INT->{return true;}
					default->{return false;}
				}
			}
			case IMAGE->{
				switch(exprType) {
					case COLOR, COLORFLOAT->{
						return true; 
					}
					default->{return false;}
				}
			}
			
			default ->{return false;}
		}
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		Type sourceType = (Type) writeStatement.getSource().visit(this, arg);
		Type destType = (Type) writeStatement.getDest().visit(this, arg);
		
		check(destType == Type.STRING || destType == Type.CONSOLE, writeStatement,
				"illegal destination type for write");
		check(sourceType != Type.CONSOLE, writeStatement, "illegal source type for write");
		return null;
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		String name = readStatement.getName(); 
		Declaration dec = symbolTable.lookup(name); 
		check(dec != null, readStatement, "undeclared var " + name); 
		check(readStatement.getSelector() == null, readStatement, "has pix selector");
		Type exprType = (Type) readStatement.getSource().visit(this,arg);
		//check(dec.getType() != exprType, readStatement, "incompatible");
		check(exprType == Type.CONSOLE || exprType == Type.STRING, readStatement, "not console");
		
		if(dec.getType() == INT)
		{	
			readStatement.getSource().setCoerceTo(dec.getType());
		}
		dec.setInitialized(true);
		return null; 
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		String name = declaration.getName();
		//set types for dimension variables if image
		if(declaration.getType()== Type.IMAGE && declaration.getDim() != null)
		{
			declaration.getDim().getHeight().setType(INT);
			declaration.getDim().getWidth().setType(INT);
		}
		boolean inserted = symbolTable.insert(name,declaration);
		check(inserted, declaration, "variable " + name + "already declared");
		Expr init = declaration.getExpr();
		if (init != null) {
			//infer type of initializer
			Type initType = (Type) init.visit(this,arg);
			//if varDec has assign init, r hs -> assignmentStmt compatible rules
			if(declaration.getOp().getStringValue().contains("="))
			{
				check(assignCompatible(declaration.getType(), initType), declaration, 
						"type of expression and declared type do not match");
				
				declaration.getExpr().setCoerceTo(declaration.getType());
				declaration.setInitialized(true);
			}
			//if varDec has read init, r hs -> readStmt compatible rules
			if(declaration.getOp().getStringValue().contains("<-"))
			{
				check(initType == Type.CONSOLE || init.getType() == Type.STRING, declaration, "not console"); 
				declaration.setInitialized(true);
			}
			
		}
		return null;
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {		
		//create synthetic vardec to be able to store program name in symbol table. 
		String name = program.getName(); 
		IToken first = program.getFirstToken(); 
		NameDef nameDef = null; 
		IToken op = null; 
		Expr expr = null; 
		VarDeclaration synthDec = new VarDeclaration(first, nameDef, op, expr); 
		symbolTable.insert(name, synthDec); 
		
		//Save root of AST so return type can be accessed in return statements
		root = program;
		
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
		
		return program;
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		String name = nameDef.getName();
		check(symbolTable.insert(name, nameDef), nameDef, "name present"); 
		return null; 
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		String name = nameDefWithDim.getName(); 
		symbolTable.insert(name, nameDefWithDim); 
		Dimension dim = nameDefWithDim.getDim();
		visitDimension(dim, arg); 
		return null; 
	}
 
	@SuppressWarnings("exports")
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		Type returnType = root.getReturnType();  //This is why we save program in visitProgram.
		returnStatement.getExpr().setType(returnType);
		Type expressionType = (Type) returnStatement.getExpr().visit(this, arg);
		check(returnType == expressionType, returnStatement, "return statement with invalid type");
		return null;
	}

	@SuppressWarnings("exports")
	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		Type expType = (Type) unaryExprPostfix.getExpr().visit(this, arg);
		check(expType == Type.IMAGE, unaryExprPostfix, "pixel selector can only be applied to image");
		unaryExprPostfix.getSelector().visit(this, arg);
		unaryExprPostfix.setType(Type.INT);
		unaryExprPostfix.setCoerceTo(COLOR);
		return Type.COLOR;
	}

}
