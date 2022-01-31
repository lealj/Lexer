package edu.ufl.cise.plc;

public class Token implements IToken {
	//constructor
	// not sure if should have line and column yet.
	public Token(Kind kind, int startPos, int length, int line, int column) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Kind getKind() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceLocation getSourceLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIntValue() {
		// TODO Auto-generated method stub
		return 0;
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
