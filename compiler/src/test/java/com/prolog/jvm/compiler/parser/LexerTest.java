package com.prolog.jvm.compiler.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.prolog.jvm.common.parser.Token;

/**
 * Test class for the lexer.
 *
 * @author Arno Bastenhof
 *
 */
public final class LexerTest {

	@Test
	public final void nextToken() throws IOException {
		// Input Strings
		String varUnderscore = "_variable";
		String varCapital = "Variable";
		String constant = "constant";
		String graphic = "#$&*+-./:<=>?@^~\\";

		// Expected tokens
		Token<PlTokenType> varUnderscoreToken = PrologTokens.getVar(varUnderscore);
		Token<PlTokenType> varCapitalToken = PrologTokens.getVar(varCapital);
		Token<PlTokenType> constantToken = PrologTokens.getAtom(constant);
		Token<PlTokenType> graphicToken = PrologTokens.getAtom(graphic);

		// Assertions
		assertEquals(new Lexer("!").nextToken(), PrologTokens.CUT);
		assertEquals(new Lexer("(").nextToken(), PrologTokens.LBRACK);
		assertEquals(new Lexer(")").nextToken(), PrologTokens.RBRACK);
		assertEquals(new Lexer(",").nextToken(), PrologTokens.COMMA);
		assertEquals(new Lexer(".").nextToken(), PrologTokens.PERIOD);
		assertEquals(new Lexer(":-").nextToken(), PrologTokens.IMPLIES);
		assertEquals(new Lexer("[]").nextToken(), PrologTokens.NIL);
		assertEquals(new Lexer(varUnderscore).nextToken(), varUnderscoreToken);
		assertEquals(new Lexer(varCapital).nextToken(), varCapitalToken);
		assertEquals(new Lexer(constant).nextToken(), constantToken);
		assertEquals(new Lexer(graphic).nextToken(), graphicToken);
	}

	@Test
	public final void ignored() throws IOException {
		String whitespace = "/**/";
		String inline = "%\r\n";
		String multiline = " \t\r\n";

		assertEquals(new Lexer(whitespace + ".").nextToken(), PrologTokens.PERIOD);
		assertEquals(new Lexer(inline + ".").nextToken(), PrologTokens.PERIOD);
		assertEquals(new Lexer(multiline + ".").nextToken(), PrologTokens.PERIOD);
	}

	@Test(expected=RuntimeException.class)
	public final void wrongSlash() throws IOException {
		new Lexer("/").nextToken();
	}

	@Test(expected=RuntimeException.class)
	public final void wrongColon() throws IOException {
		new Lexer(":").nextToken();
	}

	@Test(expected=RuntimeException.class)
	public final void wrongNil() throws IOException {
		new Lexer("[").nextToken();
	}

	@Test(expected=RuntimeException.class)
	public final void unknownChar() throws IOException {
		new Lexer("|").nextToken();
	}

}
