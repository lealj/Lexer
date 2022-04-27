package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTVisitor;


//This class eliminates hard coded dependencies on the actual Lexer class.  You can call your lexer whatever you
//want as long as it implements the ILexer interface and you have provided an appropriate body for the getLexer method.

@SuppressWarnings("exports")
public class CompilerComponentFactory {
	public static IParser getParser(String input) throws LexicalException 
	{
		return new Parser(input); 
	}
	//This method will be invoked to get an instance of your lexer.  
	public static ILexer getLexer(String input) throws LexicalException{
		return new LexerClass(input); 
	}
	public static TypeCheckVisitor getTypeChecker() {
		return new TypeCheckVisitor();
	}
	
	public static ASTVisitor getCodeGenerator(String packageName) {
	       return new CodeGenVisitor(packageName);
	}
	
}
