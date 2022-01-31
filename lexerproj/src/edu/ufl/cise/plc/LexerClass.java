package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plc.IToken.Kind;

public class LexerClass implements ILexer{
	/*
	 * Look at characters one at a time
	 * If it is a one-char token {+,*} announce token
	 * if it is = look at next char, if next is = announce '==' otherwise error
	 * if the char is non-zero digit, continue reading until non-digit found
	 * if the char is a letter, $, _, keep reading until next char is not a letter, digit, $, or _
	 */
	private enum State{START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS}
	private String _input; 
	private List<Token> tokens; 
	private int tokensIndex; 
	public LexerClass(String input) {
		_input = input;  
		tokens = new ArrayList<>();
		tokensIndex = 0; 
		
		assembleTokens(); 
	}
	
	//look into not having to add token every case, instead set kind then push?
	//test current build
	//confirm lines/cols are correct
	//work on checking next char with next func to create 2 char tokens (!=, ->, ...)
	public List<Token> assembleTokens(){
		//System.out.println("Printing...");
		char[] input_chars = _input.toCharArray(); 
		int pos = 0, line = 0; 
		State state = State.START; 
		while(true) {
			char c = input_chars[pos]; 
			Kind kind;
			switch(state) {
				case START->{
					int startPos = pos;  
					switch(c) {
						//whitespace
						case ' ', '\t', '\n', '\r'->{
							pos++;
						}
						//one key cases
						case '+'->{
							kind = Kind.PLUS; 
							//Token token = new Token(kind, startPos, 1); 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '('->{
							kind = Kind.LPAREN; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case ')'->{
							kind = Kind.RPAREN; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '['->{
							kind = Kind.LSQUARE; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case ']'->{
							kind = Kind.RPAREN; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '-'->{
							kind = Kind.MINUS; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '*'->{
							kind = Kind.TIMES; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '/'->{
							kind = Kind.DIV; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '%'->{
							kind = Kind.MOD; 
							tokens.add(new Token(kind, startPos, 1, line, pos));  
							pos++; 
						}
						case '&'->{
							kind = Kind.AND; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '|'->{
							kind = Kind.OR; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '!'->{
							kind = Kind.BANG; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '<'->{
							kind = Kind.LT; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '>'->{
							kind = Kind.GT; 
							tokens.add(new Token(kind, startPos, 1, line, pos));  
							pos++; 
						}
						case ';'->{
							kind = Kind.SEMI; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case ','->{
							kind = Kind.COMMA; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						case '^'->{
							kind = Kind.RETURN; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							pos++; 
						}
						//more than one key
						case '='->{
							state = State.HAVE_EQ; 
							pos++; 
						}
						//end
						case '0'->{
							kind = Kind.EOF; 
							tokens.add(new Token(kind, startPos, 1, line, pos)); 
							return tokens; 
						}
					}
				}
				case IN_IDENT->{
					
				}
				case HAVE_ZERO->{
					
				}
				case HAVE_DOT->{
					
				}
				case IN_FLOAT->{
								
				}
				case IN_NUM->{
					int tokenPos = pos; 
					kind = Kind.INT_LIT; 
					switch(c) {
						case '0', '1', '2', '3', '5', '6', '7', '8', '9'->{
							pos++; 
						}
						default->{
//************************* CHECK THAT POSITION IS CORRECT.
							tokens.add(new Token(kind, tokenPos, pos-tokenPos, line, pos)); 
							state = State.START; 
						}
					}
				}
				case HAVE_EQ->{
					int startPos = pos;  
					switch(c) {
						case '='->{
							kind = Kind.EQUALS; 
							tokens.add(new Token(kind, startPos, 2, line, pos));
							pos++; 
						}
						default->{throw new IllegalStateException("invalid char after '='");}
					}
				}
				case HAVE_MINUS->{
					
				}
				default->{ throw new IllegalStateException("lexer bug");}
			}
		}
	}
	
	@Override
	public IToken next() throws LexicalException {
		//return the next already computed token.
		tokensIndex++; 
		return tokens.get(tokensIndex-1);
	}

	@Override
	public IToken peek() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}

}
