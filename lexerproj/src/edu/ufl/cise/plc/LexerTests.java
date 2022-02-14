package edu.ufl.cise.plc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.plc.IToken.Kind;


public class LexerTests {

	ILexer getLexer(String input) {
		 return CompilerComponentFactory.getLexer(input);
	}
	
	//makes it easy to turn output on and off (and less typing than System.out.println)
	static final boolean VERBOSE = true;
	void show(Object obj) {
		if(VERBOSE) {
			System.out.println(obj);
		}
	}
	
	//check that this token has the expected kind
	void checkToken(IToken t, Kind expectedKind) {
		assertEquals(expectedKind, t.getKind());
	}
		
	//check that the token has the expected kind and position
	void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn){
		assertEquals(expectedKind, t.getKind());
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}
	
	//check that this token is an IDENT and has the expected name
	void checkIdent(IToken t, String expectedName){
		assertEquals(Kind.IDENT, t.getKind());
		assertEquals(expectedName, t.getText());
	}
	
	//check that this token is an IDENT, has the expected name, and has the expected position
	void checkIdent(IToken t, String expectedName, int expectedLine, int expectedColumn){
		checkIdent(t,expectedName);
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}
	
	//check that this token is an INT_LIT with expected int value
	void checkInt(IToken t, int expectedValue) {
		assertEquals(Kind.INT_LIT, t.getKind());
		assertEquals(expectedValue, t.getIntValue());	
	}
	
	//check that this token  is an INT_LIT with expected int value and position
	void checkInt(IToken t, int expectedValue, int expectedLine, int expectedColumn) {
		checkInt(t,expectedValue);
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());		
	}
	
	//check that this token is the EOF token
	void checkEOF(IToken t) {
		checkToken(t, Kind.EOF);
	}
	
	
	//The lexer should add an EOF token to the end.
	@Test
	void testEmpty() throws LexicalException {
		String input = "";
		show(input);
		ILexer lexer = getLexer(input);
		checkEOF(lexer.next());
	}
	
	//A couple of single character tokens
	@Test
	void testSingleChar0() throws LexicalException {
		String input = """
				+ 
				- 	 
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS, 0,0);
		checkToken(lexer.next(), Kind.MINUS, 1,0);
		checkEOF(lexer.next());
	}
	
	//comments should be skipped
	@Test
	void testComment0() throws LexicalException {
		//Note that the quotes around "This is a string" are passed to the lexer.  
		String input = """
				"This is a string"
				#this is a comment
				*
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 0,0);
		checkToken(lexer.next(), Kind.TIMES, 2,0);
		checkEOF(lexer.next());
	}
	
	//Example for testing input with an illegal character 
	@Test
	void testError0() throws LexicalException {
		String input = """
				abc
				@
				""";
		show(input);
		ILexer lexer = getLexer(input);
		//this check should succeed
		checkIdent(lexer.next(), "abc");
		//this is expected to throw an exception since @ is not a legal 
		//character unless it is part of a string or comment
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}
	
	//Several identifiers to test positions
	@Test
	public void testIdent0() throws LexicalException {
	String input = """
	abc
	  def
	     ghi
	""";
	show(input);
	ILexer lexer = getLexer(input);
	checkIdent(lexer.next(), "abc", 0,0);
	checkIdent(lexer.next(), "def", 1,2);
	checkIdent(lexer.next(), "ghi", 2,5);
	checkEOF(lexer.next());
	}

	
	@Test
	public void testEquals0() throws LexicalException {
		String input = """
				= == ===
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(),Kind.ASSIGN,0,0);
		checkToken(lexer.next(),Kind.EQUALS,0,2);
		checkToken(lexer.next(),Kind.EQUALS,0,5);
		checkToken(lexer.next(),Kind.ASSIGN,0,7);
		checkEOF(lexer.next());
	}

	
	@Test
	public void testIdenInt() throws LexicalException {
		String input = """
				a123 456b
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "a123", 0,0);
		checkInt(lexer.next(), 456, 0,5);
		checkIdent(lexer.next(), "b",0,8);
		checkEOF(lexer.next());
		}

	//example showing how to handle number that are too big.
	@Test
	public void testIntTooBig() throws LexicalException {
		String input = """
				42
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(),42);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();			
		});
	}
	
	// Personally created tests..............................................
	@Test
	public void testEquals0ALTERED() throws LexicalException {
		String input = """
				=
				==
				===
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(),Kind.ASSIGN,0,0);
		checkToken(lexer.next(),Kind.EQUALS,1,0);
		checkToken(lexer.next(),Kind.EQUALS,2,0);
		checkToken(lexer.next(),Kind.ASSIGN,2,2);
		checkEOF(lexer.next());
	}
	
	String getASCII(String s) {
		int[] ascii = new int[s.length()];
		for (int i = 0; i != s.length(); i++) {
			ascii[i] = s.charAt(i);
		}
		return Arrays.toString(ascii);
	}
	@Test
	public void testEscapeSequences0() throws LexicalException {
		String input = "\"\\b \\t \\n \\f \\r \"";
		show(input);
		show("input chars= " + getASCII(input));
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		show("getStringValueChars=     " + getASCII(val));
		String expectedStringValue = "\b \t \n \f \r ";
		show("expectedStringValueChars=" + getASCII(expectedStringValue));
		assertEquals(expectedStringValue, val);
		
		String text = t.getText();
		show("getTextChars=     " +getASCII(text));
		String expectedText = "\"\\b \\t \\n \\f \\r \"";
		show("expectedTextChars="+getASCII(expectedText));
		assertEquals(expectedText,text);
	}
	@Test
	public void testEscapeSequences1() throws LexicalException {
		String input = "   \" ...  \\\"  \\\'  \\\\  \"";
		show(input);
		show("input chars= " + getASCII(input));
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		show("getStringValueChars=     " + getASCII(val));
		String expectedStringValue = " ...  \"  \'  \\  ";
		show("expectedStringValueChars=" + getASCII(expectedStringValue));
		assertEquals(expectedStringValue, val);
		
		String text = t.getText();
		show("getTextChars=     " +getASCII(text));
		String expectedText = "\" ...  \\\"  \\\'  \\\\  \""; //almost the same as input, but white space is omitted
		show("expectedTextChars="+getASCII(expectedText));
		assertEquals(expectedText,text);		
	}
	
	@Test
    public void multiLineString() throws LexicalException{
        String input = """
                string a = "test
                52";
                a
                """;
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.TYPE, 0, 0); //"string"
        checkIdent(lexer.next(), "a", 0, 7);
        checkToken(lexer.next(), Kind.ASSIGN, 0, 9);
        
        checkToken(lexer.next(), Kind.STRING_LIT, 0, 11); //"\"test\n52\""
        checkToken(lexer.next(), Kind.SEMI, 1, 3);
        checkIdent(lexer.next(), "a", 2, 0);
    } 

	@Test
	void testBools() throws LexicalException {
		String input = """
				int x
				boolean
				true 
				if
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.TYPE, 0,0);
		checkToken(lexer.next(), Kind.IDENT, 0,4);
		checkToken(lexer.next(), Kind.TYPE, 1,0);
		checkToken(lexer.next(), Kind.BOOLEAN_LIT, 2,0);
		checkToken(lexer.next(), Kind.KW_IF, 3,0);
		checkEOF(lexer.next());
	}
	//Errors from project feedback tests------------------------------
	@Test
	void error1() throws LexicalException {
		String input = """
				a124.4
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0,0);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
		//checkEOF(lexer.next());
	}
	
	@Test
	void error2() throws LexicalException {
		String input = """
				.1
				""";
		show(input);
		ILexer lexer = getLexer(input);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
		//checkEOF(lexer.next());
	}
	
	@Test
	void error3() throws LexicalException {
		String input = """
				1.
				""";
		show(input);
		ILexer lexer = getLexer(input);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
		//checkEOF(lexer.next());
	}
	
	@Test
	void error4() throws LexicalException {
		String input = """
				abc#this is a comment
				123.4
				readBlue
				< ->
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0,0);
		checkToken(lexer.next(), Kind.FLOAT_LIT, 1,0);
		checkToken(lexer.next(), Kind.IDENT, 2,0);
		checkToken(lexer.next(), Kind.LT, 3, 0); 
		checkToken(lexer.next(), Kind.RARROW, 3, 2); 
		checkEOF(lexer.next());
	}
	
	@Test
	void error5() throws LexicalException {
		String input = """
				abc\\g xyz
				""";
		show(input);
		ILexer lexer = getLexer(input);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
		checkToken(lexer.next(), Kind.IDENT, 0, 6); 
		checkEOF(lexer.next());
	}
	
	@Test
	void error6() throws LexicalException {
		String input = """
				=== ====
				=! !==
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.EQUALS, 0, 0); 
		checkToken(lexer.next(), Kind.ASSIGN, 0, 2);
		checkToken(lexer.next(), Kind.EQUALS, 0, 4);
		checkToken(lexer.next(), Kind.EQUALS, 0, 6);
		
		checkToken(lexer.next(), Kind.ASSIGN, 1, 0);
		checkToken(lexer.next(), Kind.BANG, 1, 1);
		checkToken(lexer.next(), Kind.NOT_EQUALS, 1, 3);
		checkToken(lexer.next(), Kind.ASSIGN, 1, 5);
		checkEOF(lexer.next());
	}
	
	@Test
	void error7() throws LexicalException {
		String input = """
				getRED0 getREDxyz getRED+
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0, 0);
		checkToken(lexer.next(), Kind.IDENT, 0, 8);
		checkToken(lexer.next(), Kind.IDENT, 0, 18);
		checkToken(lexer.next(), Kind.PLUS, 0, 24);
		checkEOF(lexer.next());
	}
	
	@Test
	void error8() throws LexicalException {
		String input = """
				"abc def      ghi
				""";
		show(input);
		ILexer lexer = getLexer(input);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
		checkEOF(lexer.next());
	}
	
	@Test
	void error9() throws LexicalException {
		String input = """
				falsetrue truefalse !true false!true
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0, 0);
		checkToken(lexer.next(), Kind.IDENT, 0, 10);
		checkToken(lexer.next(), Kind.BANG, 0, 20);
		checkToken(lexer.next(), Kind.BOOLEAN_LIT, 0, 21);
		checkToken(lexer.next(), Kind.BOOLEAN_LIT, 0, 26);
		checkToken(lexer.next(), Kind.BANG, 0, 31);
		checkToken(lexer.next(), Kind.BOOLEAN_LIT, 0, 32);
		checkEOF(lexer.next());
	}
	
	@Test
	void error10() throws LexicalException {
		String input = """
				abc"def"hij123;
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0, 0);
		checkToken(lexer.next(), Kind.STRING_LIT, 0, 3);
		checkToken(lexer.next(), Kind.IDENT, 0, 8);
		checkToken(lexer.next(), Kind.SEMI, 0, 14);
		checkEOF(lexer.next());
	}
	
	@Test
	void error11() throws LexicalException {
		String input = """
				;,;";,;"
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.SEMI, 0, 0);
		checkToken(lexer.next(), Kind.COMMA, 0, 1);
		checkToken(lexer.next(), Kind.SEMI, 0, 2);
		checkToken(lexer.next(), Kind.STRING_LIT, 0, 3);
		checkEOF(lexer.next());
	}
	
	@Test
	void error12() throws LexicalException {
		String input = """
				
				getREDgetBLUE
				getRED+getBLUE
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 1, 0);
		checkToken(lexer.next(), Kind.IDENT, 2, 0);
		checkToken(lexer.next(), Kind.PLUS, 2, 6);
		checkToken(lexer.next(), Kind.IDENT, 2, 7);
		checkEOF(lexer.next());
	}
	
	@Test
	void error13() throws LexicalException {
		String input = """
				123 "this is a' string with quote in the middle"456
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.INT_LIT, 0, 0);
		checkToken(lexer.next(), Kind.STRING_LIT, 0, 4);
		checkToken(lexer.next(), Kind.INT_LIT, 0, 48);
		checkEOF(lexer.next());
	}


}
