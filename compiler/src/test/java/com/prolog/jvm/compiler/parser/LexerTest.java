package com.prolog.jvm.compiler.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.prolog.jvm.compiler.parser.Lexer;
import com.prolog.jvm.compiler.parser.Token;
import com.prolog.jvm.compiler.parser.TokenType;

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
		Token varUnderscoreToken = new Token(TokenType.VAR, varUnderscore);
		Token varCapitalToken = new Token(TokenType.VAR, varCapital);
		Token constantToken = new Token(TokenType.ATOM, constant);
		Token graphicToken = new Token(TokenType.ATOM, graphic);

		// Assertions
		assertEquals(new Lexer("!").nextToken(), Token.CUT);
		assertEquals(new Lexer("(").nextToken(), Token.LBRACK);
		assertEquals(new Lexer(")").nextToken(), Token.RBRACK);
		assertEquals(new Lexer(",").nextToken(), Token.COMMA);
		assertEquals(new Lexer(".").nextToken(), Token.PERIOD);
		assertEquals(new Lexer(":-").nextToken(), Token.IMPLIES);
		assertEquals(new Lexer("[]").nextToken(), Token.NIL);
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

		assertEquals(new Lexer(whitespace + ".").nextToken(), Token.PERIOD);
		assertEquals(new Lexer(inline + ".").nextToken(), Token.PERIOD);
		assertEquals(new Lexer(multiline + ".").nextToken(), Token.PERIOD);
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
