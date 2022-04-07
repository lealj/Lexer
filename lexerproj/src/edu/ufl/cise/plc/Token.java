package edu.ufl.cise.plc;

public class Token implements IToken {
	//(delete comment) high priority on get kind and get source for testing
	//constructor
	// not sure if should have line and column yet.
	private Kind kind; 
	private String src, text;
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
		return this.kind;
	}

	@Override
	public String getText() {
		//white space remover
		if(src.charAt(0) == ' ') {
			while(src.charAt(0) == ' ')
			{
				src = src.substring(1, src.length()); 
			}
		}
		if(Character.isLetter(src.charAt(0)))
		{
			if(!Character.isLetter(src.charAt(src.length()-1)) 
					&& !Character.isDigit(src.charAt(src.length()-1)))
			{
				src = src.substring(0,src.length()-1); 
			}
		}
		
		return src;
	}

	@Override
	public SourceLocation getSourceLocation() {
		return new SourceLocation(line, column);
	}

	@Override
	public int getIntValue() {
		try {
			Integer.parseInt(src); 
		}
		catch(NumberFormatException e)
		{
			if(Character.isDigit(src.charAt(src.length()-1)) == false)
			{
				src = src.substring(0, src.length()-1); 
			}
		}
		return Integer.parseInt(src); 
	}

	@Override
	public float getFloatValue() {
		try {
			Float.parseFloat(src); 
		}
		catch(NumberFormatException e)
		{
			if(Character.isDigit(src.charAt(src.length()-1)) == false)
			{
				src = src.substring(0, src.length()-1); 
			}
		}
		return Float.parseFloat(src);
	}

	@Override
	public boolean getBooleanValue() {
		if(src.equals("true") || src.equals("TRUE"))
			return true; 
		return false;
	}

	@Override
	public String getStringValue() {
		String ret = ""; 
		if(src.charAt(0) == ' ')
		{
			while(src.charAt(0) == ' ')
			{
				this.src = src.substring(1, src.length()); 
			}
		}
		for(int i = 0; i < src.length(); i++)
		{
			char c = src.charAt(i); 
			switch(c) {
				case ' '->{
					ret += " "; 
				}
				case '.'->{
					ret+="."; 
				}
				case '\\'->{
					switch(src.charAt(i+1)) {
						case 't'->{
							ret += "\t"; 
						}
						case 'b'->{
							ret += "\b";
						}
						case 'n'->{
							ret += "\n";
						}
						case 'f'->{
							ret += "\f";
						}
						case 'r'->{
							ret += "\r";
						}
						case '\"'->{
							ret += "\""; 
						}
						case '\\'->{
							ret += "\\"; 
						}
						case '\''->{
							ret += "\'"; 
						}
					}
				}
				case '\"'->{
					continue; 
				}
				default->{
					ret += c; 
				}
			}
		}
		int ind = ret.length()-1; 
		if(ret.charAt(ind) == ')' || ret.charAt(ind) == ';' || ret.charAt(ind)==',')
		{
			ret = ret.substring(0, ret.length()-1); 
		}
		return ret; 
	}
	
	public void setText(String text) {
		this.text = text; 
	}
}


