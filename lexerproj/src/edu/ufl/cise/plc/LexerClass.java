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
	//make private
	public List<Token> tokens; 
	private int tokensIndex; 
	public LexerClass(String input){
		_input = input; 
		_input += '0'; 
		tokens = new ArrayList<>();
		tokensIndex = 0; 
		
		assembleTokens(); 
	}

	//TO DO: 
		//look into not having to add token every case, instead set kind then push?
		//test current build
		//confirm lines/cols are correct
		//work on checking next char with next func to create 2 char tokens (!=, ->, ...)
		//add eof token to end. 
	
	//TEST PASSED: 
		//testEquals0, testEmpty, testSingleChar, testIntTooBig
	
	public List<Token> assembleTokens(){
		//int stuck = 0; 
		//System.out.println("Printing...");
		char[] input_chars = _input.toCharArray(); 
		int pos = 0, line = 0, col=0; 
		State state = State.START;
		//txt to send to token class to decode 
		String src = ""; 
		int tokensSize = 0; 
		while(true) {
			//if token was added reset src string
			//System.out.println(stuck);
			//stuck++; 
			if(tokensSize < tokens.size())
			{
				tokensSize = tokens.size(); 
				src = ""; 
			}
			char c = input_chars[pos]; 
			if(" \t\n\r".contains(String.valueOf(c)) == false)
				src += c; 
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
							//Token token = new Token(kind, startPos, 1); 
							tokens.add(new Token(kind, src, startPos, 1, line, col)); 
							pos++; col++;
						}
						case '('->{
							kind = Kind.LPAREN; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));  
							pos++; col++;
						}
						case ')'->{
							kind = Kind.RPAREN; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case '['->{
							kind = Kind.LSQUARE; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case ']'->{
							kind = Kind.RPAREN; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case '-'->{
							kind = Kind.MINUS; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case '*'->{
							kind = Kind.TIMES; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case '/'->{
							kind = Kind.DIV; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case '%'->{
							kind = Kind.MOD; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));    
							pos++; col++;
						}
						case '&'->{
							kind = Kind.AND; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case '|'->{
							kind = Kind.OR; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case '!'->{
							kind = Kind.BANG; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case '<'->{
							kind = Kind.LT; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case '>'->{
							kind = Kind.GT; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));    
							pos++; col++;
						}
						case ';'->{
							kind = Kind.SEMI; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case ','->{
							kind = Kind.COMMA; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						case '^'->{
							kind = Kind.RETURN; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
							pos++; col++;
						}
						//possibly more than one key
						case '='->{
							state = State.HAVE_EQ; 
							pos++; col++;
						}
						//integer expected
						case '1','2','3','4','5','6','7','8', '9'->{
							state = State.IN_NUM; 
							pos++; col++; 
						}
						//end (EOF)
						case '0'->{
							kind = Kind.EOF; 
							tokens.add(new Token(kind, src, startPos, 1, line, col));   
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
							pos++; col++; 
						}
						default->{
							tokens.add(new Token(kind, src,tokenPos, pos-tokenPos, line, col)); 
							state = State.START; 
						}
					}
				}
				
				case HAVE_EQ->{
					int startPos = pos-1;  
					switch(c) {
						case '='->{
							kind = Kind.EQUALS; 
							tokens.add(new Token(kind, src,startPos, 2, line, startPos));
							pos++; col++; 
							state = State.START; 
						}
						//if equal to case, we have "= "|"=\n"|etc., token.kind->assign
						case ' ', '\t', '\n', '\r'->{
							kind = Kind.ASSIGN; 
							tokens.add(new Token(kind, src,startPos, 1, line, startPos));
							//reset state so to not get stuck in HAVE_EQ case. 
							//no need to increment
							state = State.START; 
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
		Token t = tokens.get(tokensIndex-1); 
		//test int for format/length errors
		if(t.getKind()== Kind.INT_LIT)
		{
			try {
				t.getIntValue(); 
			}
			catch(NumberFormatException e)
			{
				throw new LexicalException("int too big"); 
			}
		}
		
		return tokens.get(tokensIndex-1);
	}

	@Override
	public IToken peek() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}

}
