package edu.ufl.cise.plc;

public class CodeGenStringBuilder {
	
	StringBuilder delegate;
	public CodeGenStringBuilder() {
		delegate = new StringBuilder();
	}
	
	public CodeGenStringBuilder append(String s){
		delegate.append(s);
		return this; 
	}
	
	public CodeGenStringBuilder cast(String castTo){
		delegate.append("(");
		delegate.append(castTo); 
		delegate.append(")"); 
		return this; 
	}
	
	public CodeGenStringBuilder comma(){
		delegate.append(",");
		return this; 
	}
	
	public CodeGenStringBuilder semi(){
		delegate.append(";");
		return this; 
	}

	public CodeGenStringBuilder newline(){
		delegate.append("\n");
		return this; 
	}
	
	public CodeGenStringBuilder equal(){
		delegate.append("=");
		return this; 
	}
	
	public CodeGenStringBuilder space(){
		delegate.append(" ");
		return this; 
	}
	
	public CodeGenStringBuilder lparen(){
		delegate.append("(");
		return this; 
	}
	
	public CodeGenStringBuilder rparen(){
		delegate.append(")");
		return this; 
	}
	
	public CodeGenStringBuilder ques(){
		delegate.append("?");
		return this; 
	}
	
	public CodeGenStringBuilder colon(){
		delegate.append(":");
		return this; 
	}
	
	public CodeGenStringBuilder tripleQuote(){
		delegate.append("\"\"\"");
		return this; 
	}
	
	
}
