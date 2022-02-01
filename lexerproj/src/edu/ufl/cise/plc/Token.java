package edu.ufl.cise.plc;

public class Token implements IToken {
	//(delete comment) high priority on get kind and get source for testing
	//constructor
	// not sure if should have line and column yet.
	private Kind kind; 
	private String src;
	private int startPos, length, line, column; 
	public Token(Kind kind, String src, int startPos, int length, int line, int column) {
		// TODO Auto-generated constructor stub
		this.kind = kind; 
		this.src = src; 
		this.startPos = startPos; 
		this.length = length; 
		this.line = line; 
		this.column = column; 
	}

	@Override
	public Kind getKind() {
		// TODO Auto-generated method stub
		return this.kind;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceLocation getSourceLocation() {
		return new SourceLocation(line, column);
	}

	@Override
	public int getIntValue() {
		return Integer.parseInt(src); 
	}

	@Override
	public float getFloatValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getBooleanValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getStringValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
