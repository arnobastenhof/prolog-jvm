package com.prolog.jvm.compiler.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import com.prolog.jvm.exceptions.RecognitionException;

/**
 * Test class for the lexer.
 *
 * @author Arno Bastenhof
 *
 */
public final class LexerTest {

	// String constants for testing the lexer with
	private static final String VAR_UNDERSCORE = "_variable";
	private static final String VAR_CAPITAL = "Variable";
	private static final String CONSTANT = "constant";
	private static final String GRAPHIC = "#$&*+-./:<=>?@^~\\";
	private static final String WHITESPACE = "/**/";
	private static final String INLINE_COMMENT = "%\r\n";
	private static final String MULTILINE_COMMENT = " \t\r\n";

	@Test
	public final void nextToken() throws IOException, RecognitionException {
		// Expected tokens
		Token varUnderscoreToken = Tokens.getVar(VAR_UNDERSCORE);
		Token varCapitalToken = Tokens.getVar(VAR_CAPITAL);
		Token constantToken = Tokens.getAtom(CONSTANT);
		Token graphicToken = Tokens.getAtom(GRAPHIC);

		// Assertions
		expectMatch("(", Tokens.LBRACK);
		expectMatch(")", Tokens.RBRACK);
		expectMatch(",", Tokens.COMMA);
		expectMatch(".", Tokens.PERIOD);
		expectMatch(":-", Tokens.IMPLIES);
		expectMatch("[]", Tokens.NIL);
		expectMatch(VAR_UNDERSCORE, varUnderscoreToken);
		expectMatch(VAR_CAPITAL, varCapitalToken);
		expectMatch(CONSTANT, constantToken);
		expectMatch(GRAPHIC, graphicToken);
		expectMatch(WHITESPACE + ".", Tokens.PERIOD);
		expectMatch(INLINE_COMMENT + ".", Tokens.PERIOD);
		expectMatch(MULTILINE_COMMENT + ".", Tokens.PERIOD);
	}

	@Test(expected=RecognitionException.class)
	public final void wrongSlash() throws IOException, RecognitionException {
		expectException("/");
	}

	@Test(expected=RecognitionException.class)
	public final void wrongColon() throws IOException, RecognitionException {
		expectException(":");
	}

	@Test(expected=RecognitionException.class)
	public final void wrongNil() throws IOException, RecognitionException {
		expectException("[");
	}

	@Test(expected=RecognitionException.class)
	public final void unknownChar() throws IOException, RecognitionException {
		expectException("|");
	}

	private void expectMatch(String input, Token expected) throws IOException, RecognitionException {
		try (Reader reader = new StringReader(input)) {
			Token token = PrologLexer.newInstance(reader).nextToken();
			assertEquals(token, expected);
		}
	}

	private void expectException(String input) throws IOException, RecognitionException {
		try (Reader reader = new StringReader(input)) {
			PrologLexer.newInstance(reader).nextToken();
		}
	}

}
