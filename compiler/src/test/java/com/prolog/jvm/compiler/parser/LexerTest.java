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
		assertEquals(new PrologLexer("!").nextToken(), PrologTokens.CUT);
		assertEquals(new PrologLexer("(").nextToken(), PrologTokens.LBRACK);
		assertEquals(new PrologLexer(")").nextToken(), PrologTokens.RBRACK);
		assertEquals(new PrologLexer(",").nextToken(), PrologTokens.COMMA);
		assertEquals(new PrologLexer(".").nextToken(), PrologTokens.PERIOD);
		assertEquals(new PrologLexer(":-").nextToken(), PrologTokens.IMPLIES);
		assertEquals(new PrologLexer("[]").nextToken(), PrologTokens.NIL);
		assertEquals(new PrologLexer(varUnderscore).nextToken(), varUnderscoreToken);
		assertEquals(new PrologLexer(varCapital).nextToken(), varCapitalToken);
		assertEquals(new PrologLexer(constant).nextToken(), constantToken);
		assertEquals(new PrologLexer(graphic).nextToken(), graphicToken);
	}

	@Test
	public final void ignored() throws IOException {
		String whitespace = "/**/";
		String inline = "%\r\n";
		String multiline = " \t\r\n";

		assertEquals(new PrologLexer(whitespace + ".").nextToken(), PrologTokens.PERIOD);
		assertEquals(new PrologLexer(inline + ".").nextToken(), PrologTokens.PERIOD);
		assertEquals(new PrologLexer(multiline + ".").nextToken(), PrologTokens.PERIOD);
	}

	@Test(expected=RuntimeException.class)
	public final void wrongSlash() throws IOException {
		new PrologLexer("/").nextToken();
	}

	@Test(expected=RuntimeException.class)
	public final void wrongColon() throws IOException {
		new PrologLexer(":").nextToken();
	}

	@Test(expected=RuntimeException.class)
	public final void wrongNil() throws IOException {
		new PrologLexer("[").nextToken();
	}

	@Test(expected=RuntimeException.class)
	public final void unknownChar() throws IOException {
		new PrologLexer("|").nextToken();
	}

}
