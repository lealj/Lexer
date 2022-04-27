package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ufl.cise.plc.IToken.Kind;

public class LexerClass implements ILexer{
	/*
	 * Look at characters one at a time
	 * If it is a one-char token {+,*} announce token
	 * if it is = look at next char, if next is = announce '==' otherwise error
	 * if the char is non-zero digit, continue reading until non-digit found
	 * if the char is a letter, $, _, keep reading until next char is not a letter, digit, $, or _
	 */
	private enum State{START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, 
			HAVE_EQ, HAVE_MINUS, HAVE_LARROW, HAVE_RARROW, HAVE_BANG, HAVE_STRING, HAVE_COMMENT}
	private String _input; 
	public List<Token> tokens; 
	private Map<String, Kind> kindMap; 
	private int tokensIndex; 
	public LexerClass(String input) throws LexicalException{
		_input = input; 
		_input += '0'; 
		_input += '\n'; 
		tokens = new ArrayList<>();
		kindMap = new HashMap<>(); 
		tokensIndex = 0; 
		
		populateKindMap(); 
		assembleTokens(); 
	}

	//TO DO: 
		//look into not having to add token every case, instead set kind then push?
		//work on checking next char with next func to create 2 char tokens (!=, ->, ...)
		//add eof token to end. 
	
	//TEST PASSED: 
		//testEquals0, testEmpty, testSingleChar, testIntTooBig, idenInt, 
	
	private void populateKindMap() {
		//key words
		kindMap.put("if", Kind.KW_IF);
		kindMap.put("fi", Kind.KW_FI);
		kindMap.put("else", Kind.KW_ELSE);
		kindMap.put("write", Kind.KW_WRITE);
		kindMap.put("console", Kind.KW_CONSOLE);
		kindMap.put("void", Kind.KW_VOID);
		//bool
		kindMap.put("true", Kind.BOOLEAN_LIT);
		kindMap.put("false", Kind.BOOLEAN_LIT);
		//colors
		kindMap.put("BLACK", Kind.COLOR_CONST);
		kindMap.put("BLUE", Kind.COLOR_CONST);
		kindMap.put("CYAN", Kind.COLOR_CONST);
		kindMap.put("DARK_GRAY", Kind.COLOR_CONST);
		kindMap.put("GRAY", Kind.COLOR_CONST);
		kindMap.put("GREEN", Kind.COLOR_CONST);
		kindMap.put("LIGHT_GRAY", Kind.COLOR_CONST);
		kindMap.put("MAGENTA", Kind.COLOR_CONST);
		kindMap.put("ORANGE", Kind.COLOR_CONST);
		kindMap.put("PINK", Kind.COLOR_CONST);
		kindMap.put("RED", Kind.COLOR_CONST);
		kindMap.put("WHITE", Kind.COLOR_CONST);
		kindMap.put("YELLOW", Kind.COLOR_CONST); 
		//types
		kindMap.put("int", Kind.TYPE);
		kindMap.put("float", Kind.TYPE);
		kindMap.put("string", Kind.TYPE);
		kindMap.put("boolean", Kind.TYPE);
		kindMap.put("color", Kind.TYPE);
		kindMap.put("image", Kind.TYPE);
		//colorOP
		kindMap.put("getRed", Kind.COLOR_OP);
		kindMap.put("getGreen", Kind.COLOR_OP);
		kindMap.put("getBlue", Kind.COLOR_OP);
		//imageOP
		kindMap.put("getWidth", Kind.IMAGE_OP);
		kindMap.put("getHeight", Kind.IMAGE_OP);
	
	}

	private List<Token> assembleTokens(){
		char[] input_chars = _input.toCharArray(); 
		int pos = 0, line = 0, col=0; 
		State state = State.START;
		//txt to send to token class to decode 
		String src = ""; 
		int tokensSize = 0; 
		boolean newLine_inString = false; 
		while(true) {
			//if token was added reset src string
			if(tokensSize < tokens.size())
			{
				tokensSize = tokens.size(); 
				src = "";  
				if(newLine_inString == true)
				{
					int stringCol = tokens.get(tokens.size()-1).getSourceLocation().column(); 
					int nl_pos = tokens.get(tokens.size()-1).getStringValue().indexOf('\n', stringCol);
					col = pos - nl_pos; 
					line++; 
					newLine_inString = false; 
				}
				
			}
			char c = input_chars[pos]; 
			Kind kind;
			switch(state) {
				case START->{
					int startPos = pos; 
					switch(c) {
						//whitespace
						case ' ', '\t', '\n', '\r'->{
							pos++; col++; 
							if(c == '\n' || c =='\r')
							{
								col = 0; line++; 
							}
						}
						//one key cases
						case '+'->{
							kind = Kind.PLUS; 
							tokens.add(new Token(kind, "+", startPos, 1, line, col)); 
							pos++; col++;
						}
						case '('->{
							kind = Kind.LPAREN; 
							tokens.add(new Token(kind, "(", startPos, 1, line, col));  
							pos++; col++;
						}
						case ')'->{
							kind = Kind.RPAREN; 
							tokens.add(new Token(kind, ")", startPos, 1, line, col));   
							pos++; col++;
						}
						case '['->{
							kind = Kind.LSQUARE; 
							tokens.add(new Token(kind, "[", startPos, 1, line, col));   
							pos++; col++;
						}
						case ']'->{
							kind = Kind.RSQUARE; 
							tokens.add(new Token(kind, "]", startPos, 1, line, col));   
							pos++; col++;
						}
						case '*'->{
							kind = Kind.TIMES; 
							tokens.add(new Token(kind, "*", startPos, 1, line, col));   
							pos++; col++;
						}
						case '/'->{
							kind = Kind.DIV; 
							tokens.add(new Token(kind, "/", startPos, 1, line, col));   
							pos++; col++;
						}
						case '%'->{
							kind = Kind.MOD; 
							tokens.add(new Token(kind, "%", startPos, 1, line, col));    
							pos++; col++;
						}
						case '&'->{
							kind = Kind.AND; 
							tokens.add(new Token(kind, "&", startPos, 1, line, col));   
							pos++; col++;
						}
						case '|'->{
							kind = Kind.OR; 
							tokens.add(new Token(kind, "|", startPos, 1, line, col));   
							pos++; col++;
						}
						case ';'->{
							kind = Kind.SEMI; 
							tokens.add(new Token(kind, ";", startPos, 1, line, col));  
							pos++; col++;
							
						}
						case ','->{
							kind = Kind.COMMA; 
							tokens.add(new Token(kind, ",", startPos, 1, line, col));   
							pos++; col++;
						}
						case '^'->{
							kind = Kind.RETURN; 
							tokens.add(new Token(kind, "^", startPos, 1, line, col));   
							pos++; col++;
						}
						//possibly more than one key
						case '='->{
							state = State.HAVE_EQ; 
							src = "=";
							pos++; col++; 
						}
						case '!'->{
							state = State.HAVE_BANG;  
							src = "!";
							pos++; col++;
						}
						case '<'->{
							state = State.HAVE_LARROW; 
							src = "<";
							pos++; col++;
						}
						case '>'->{
							state = State.HAVE_RARROW; 
							src = ">";
							pos++; col++;
						}
						case '-'->{
							state = State.HAVE_MINUS;  
							src = "-";
							pos++; col++;
						}
						case '"'->{
							state = State.HAVE_STRING; 
							src = "\"";
							pos++;  col++;
						}
						case '#'->
						{
							state = State.HAVE_COMMENT; 
						}
						//integer expected
						case '1','2','3','4','5','6','7','8', '9'->{
							state = State.IN_NUM; 
							src += c;
							pos++;  
						}
						case '0'->{
							state = State.HAVE_ZERO; 
							src = "0";
							pos++; col++; 
						}
						default->{
							if(Character.isJavaIdentifierStart(c))
							{
								state = State.IN_IDENT; 
								src += c;
								pos++;  
							}
							else {
								kind = Kind.ERROR; 
								tokens.add(new Token(kind, src, 1, 1, line, col)); 
								pos++; 
							}
						}
					}
				}
				case HAVE_COMMENT->
				{
					switch (c)
					{
						case '\n','\r'->{
							state = State.START; 
							src = ""; 
						}
						default->{
							pos++; 
						}
					}
				}
				case HAVE_STRING->
				{
					int startPos = col-1; 
					switch (c) {
						case ' '->{
							src += ' '; 
							pos++; 
						}
						case '"'->{
							src += "\"";
							if(_input.charAt(pos+1) != '"' && _input.charAt(pos-1) != '\\')
							{
								kind = Kind.STRING_LIT; 
								Token t = new Token(kind, src, 1, 1, line, startPos);
								col += src.length()-1;
								src = ""; 
								t.setText(_input); 
								tokens.add(t); 
								state = State.START;
							}
							
							pos++; 
						}
						case '\n'->{
							newLine_inString = true;
							pos++; 
						}
						default->{ 
							src += c; 
							pos++; 
						}
					}
				}
				case IN_IDENT->{ 
					if(Character.isJavaIdentifierPart(c)) {
						pos++;
						src += c; 
					}
					else {
						//iterate through reserved word map
						int ind = src.length()-1; 
						if(src.charAt(ind)==';' || src.charAt(ind)=='[')
						{
							src = src.substring(0,src.length()-1); 
						}
						if(kindMap.containsKey(src) == true) {
							kind = kindMap.get(src); 
							tokens.add(new Token(kind, src, pos-src.length(), src.length(), line, col));
							state = State.START;
							col += src.length();
							src = "";
						}
						else {
							if(c != '\"' && c != '%' && c != '/' && c != '*' && c!= '+' && c!='-'
									&& c!= '\n' && c!=' ' && c!=';' && c!='#' && c!='(' && c!=')'
									&& c!=',' && c!='[' && c!=']' && !Character.isDigit(c))
							{
								if(c == '.')
								{
									tokens.add(new Token(Kind.IDENT, src, 0,0,0,0));
								}
								pos = pos+1; 
								src += c; 
								c = input_chars[pos]; 
								while(Character.isJavaIdentifierPart(c))
								{
									pos++;
									c = input_chars[pos];
									src += c; 
								}
								
								tokens.add(new Token(Kind.ERROR, "unused", 0, 0, 0, 0));
								col += src.length(); 
								src = ""; 
								state = State.START;
							}
							else {
								kind = Kind.IDENT; 
								tokens.add(new Token(kind, src, pos-src.length(), src.length(), line, col));
								state = State.START;
								col += src.length(); 
								src = "";
							}
						}
					}
				}
				//test
				case HAVE_ZERO->{
					int startPos = pos-1; 
					switch(c)
					{
						case '.'->{
							state = State.HAVE_DOT; 
							pos++; //col++; 
							src += c; 
						}
						case '\n','\r'->{
							kind = Kind.EOF; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));  
							src = "";
							return tokens; 
						}
						default->{
							kind = Kind.INT_LIT; 
							tokens.add(new Token(kind, src, startPos, src.length(), line, pos));
							state = State.START; 
							src = ""; 
						}
					}
				}
				//add default/ test
				case HAVE_DOT->{
					switch(c)
					{
						case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'->{
							state = State.IN_FLOAT; 
							//src += c; 
						}
						default->{
							tokens.add(new Token(Kind.ERROR, "", 0, 0, 0, 0));
							src = "";
							state = State.START;
						}
					}
				}
				
				case IN_FLOAT->{
					int tokenPos = pos; 
					kind = Kind.FLOAT_LIT; 
					switch(c) {
						case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'->{
							pos++;
							src += c; 
						}
						default->{
							tokens.add(new Token(kind, src,tokenPos, pos-tokenPos, line, col)); 
							col+=src.length();
							src = "";
							state = State.START; 
						}
					}			
				}
				
				case IN_NUM->{
					kind = Kind.INT_LIT; 
					switch(c) {
						case '0', '1', '2', '3','4', '5', '6', '7', '8', '9'->{
							pos++; 
							src += c; 
						}
						case '.'->{
							pos++; 
							src += c; 
							state = State.HAVE_DOT;
						}
						case ' ', '\t', '\n', '\r'->{
							tokens.add(new Token(kind, src, pos-src.length(), src.length(), line, col));
							col+= src.length();
							src = ""; 
							state = State.START; 
						}
						default->{
							if(Character.isDigit(c) == false) {
								tokens.add(new Token(kind, src,pos-src.length(), src.length(), line, col));
								col += src.length(); 
								src = "";
								state = State.START; 
							}
						}
					}
				}
				
				case HAVE_EQ->{
					int startPos = col-1; 
					switch(c) {
						case '='->{
							kind = Kind.EQUALS; 
							src += '='; 
							pos++; col++; 
						}
						//if equal to case, we have "= "|"=\n"|etc., token.kind->assign
						case ' ', '\t', '\n', '\r'->{
							kind = Kind.ASSIGN; 
						}
						default->{kind = Kind.ASSIGN;}
					}
					tokens.add(new Token(kind, src,startPos, src.length(), line, startPos));
					state = State.START; 
				}
				// test
				case HAVE_MINUS->{
					int startPos = col-1; 
					switch(c) {
						case '>'->{
							kind = Kind.RARROW; 
							src += '>'; 
							pos++; col++; 
						}
						case ' ', '\t', '\n', '\r'->{
							kind = Kind.MINUS; 
						}
						default->{
								kind = Kind.MINUS;
							}
					}
					tokens.add(new Token(kind, src, startPos, src.length(), line, startPos));
					state = State.START; 
				}
				// test
				case HAVE_LARROW->{
					int startPos = col-1; 
					kind = Kind.ERROR;
					switch(c) {
						case '-'->{
							kind = Kind.LARROW;
							src += c; 
						}
						
						case '='->{
							kind = Kind.LE; 
							src += c; 
						}
						
						case '<'-> {
							kind = Kind.LANGLE; 
							src += c; 
						}
						
						case ' ', '\t', '\n', '\r'->{
							kind = Kind.LT; 
						}
					} 
					if(kind != Kind.LT)
					{
						pos++; col++; 
					}
					tokens.add(new Token(kind, src, startPos, src.length(), line, startPos));
					src = ""; 
					state = State.START; 
				}
				// test
				case HAVE_RARROW->{
					int startPos = col-1; 
					kind = Kind.ERROR;
					switch(c) {
						case '='-> {
							kind = Kind.GE; 
							src += c; 
						}
						
						case '>'-> {
							kind = Kind.RANGLE; 
							src += c; 
						}
						
						case ' ', '\t', '\n', '\r'-> kind = Kind.GT; 
	
					} 
					
					if(kind != Kind.GT)
					{
						pos++; col++; 
					}
					tokens.add(new Token(kind, src, startPos, src.length(), line, startPos));
					src = ""; 
					state = State.START; 
				}
				//test
				case HAVE_BANG->{
					int startPos = col-1; 
					kind = Kind.BANG; 
					System.out.println(c); 
					switch(c) {
						case '='-> {
							kind = Kind.NOT_EQUALS; 
							pos++;  col++; 
							src += c; 
							tokens.add(new Token(kind, src, startPos, src.length(), line, startPos));
							src = ""; 
							state = State.START;
						}
						
						default->{
							kind = Kind.BANG; 
							tokens.add(new Token(kind, src, startPos, src.length(), line, startPos));
							state = State.START;
						}
					}
				}
				default->{ throw new IllegalStateException("lexer bug");}
			}
		}
	}
	
	@Override
	public IToken next() throws LexicalException {
		//return the next already computed token.
		tokensIndex++; 
		Token t = tokens.get(tokensIndex-1); 
		//test int for format/length errors
		if(t.getKind() == Kind.INT_LIT)
		{
			try {
				t.getIntValue(); 
			}
			catch(NumberFormatException e)
			{
				throw new LexicalException("int too big"); 
			}
		}
		else if(t.getKind() == Kind.ERROR)
		{
			throw new LexicalException("unused"); 
		}
		return tokens.get(tokensIndex-1);
	}

	@Override
	public IToken peek() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}

}

