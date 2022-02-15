package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;

public interface IParser {
	@SuppressWarnings("exports")
	ASTNode parse() throws PLCException;
	
}
