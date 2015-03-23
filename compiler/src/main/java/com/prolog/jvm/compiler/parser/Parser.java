package com.prolog.jvm.compiler.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * A parser for a subset of Prolog, based on Covington (1993), ISO Prolog:
 * A Summary of the Draft Proposed Standard. The implementation of this class is
 * based on Pattern 3 of Parr (2010), Language Implementation Patterns:
 * LL(1) Recursive-Descent Parser.
 *
 * TODO: Deviations from proposed ISO standard
 *
 * @author Arno Bastenhof
 *
 */
public final class Parser {

	// Strings used to compose error messages.
	private static final String UNEXPECTED_TOKEN = "Unexpected token ";
	private static final String AT_LINE = " at line ";
	private static final String OR = ", or ";
	private static final String EXPECTED = " Expected type ";

	private final Lexer input;
	Token lookahead; // package-private for testing purposes

	/**
	 * Static factory method for obtaining a new <code>Parser</code> instance
	 * based on an input String.
	 *
	 * @throws IOException
	 */
	public static final Parser newInstance(String input) throws IOException {
		return newInstance(new StringReader(input));
	}

	/**
	 * Static factory method for obtaining a new <code>Parser</code> instance
	 * based on an <code>InputStream</code>.
	 *
	 * @throws IOException
	 */
	public static final Parser newInstance(InputStream input) throws IOException {
		return newInstance(new InputStreamReader(input));
	}

	private static final Parser newInstance(Reader input) throws IOException {
		Lexer lexer = new Lexer(input);
		Parser parser = new Parser(lexer);
		return parser;
	}

	/**
	 *
	 * @param input
	 */
	public Parser(Lexer input) {
		this.input = input;
	}

	/**
	 * Parses a Prolog program, consisting of a sequence of one or more program
	 * clauses, being either facts (e.g., <code>father(zeus,ares).</code>) or rules
	 * (<code>grandparent(X,Y) :- parent(X,Z), parent(Z,Y).</code>).
	 *
	 * @throws IOException
	 */
	// program = {clause}- ;
	public void program() throws IOException {
		consume(); // Read the first token.
		do {
			clause();
		}
		while (this.lookahead.getType() == TokenType.ATOM);
		match(TokenType.EOF);
	}

	/**
	 * Parses a query, consisting of a non-empty sequence of goals, the latter in
	 * turn being terms built from a functor (possibly of arity 0) and an optional
	 * list of term arguments.
	 *
	 * @throws IOException
	 */
	// query = structure, {",", structure}, "." ;
	public void query() throws IOException {
		consume(); // Read the first token.
		structure();
		while (this.lookahead.getType() == TokenType.COMMA) {
			consume();
			structure();
		}
		match(TokenType.PERIOD);
		match(TokenType.EOF);
	}

	// clause = structure, [":-", structure, {",", structure} ], "."
	private void clause() throws IOException {
		structure();
		if (this.lookahead.getType() == TokenType.IMPLIES) {
			consume();
			structure();
			while (this.lookahead.getType() == TokenType.COMMA) {
				consume();
				structure();
			}
		}
		match(TokenType.PERIOD);
	}

	// term = "[]" | variable | structure ;
	private void term() throws IOException {
		switch (this.lookahead.getType()) {
		case VAR:
			consume();
			break;
		case ATOM:
			structure();
			break;
		case NIL:
			consume();
			break;
		default:
			throw new RuntimeException(getErrorMsg(
					TokenType.VAR, TokenType.ATOM, TokenType.NIL));
		}
	}

	// structure = atom, ["(", term, {",", term}, ")"] ;
	private void structure() throws IOException {
		match(TokenType.ATOM);
		if (this.lookahead.getType() == TokenType.LBRACK) {
			consume();
			term();
			while (this.lookahead.getType() == TokenType.COMMA) {
				consume();
				term();
			}
			match(TokenType.RBRACK);
		}
	}

	// === Token consumption ===

	private void match(TokenType type) throws IOException {
		if (this.lookahead.getType() == type) {
			consume();
		}
		else {
			throw new RuntimeException(getErrorMsg(type));
		}
	}

	private void consume() throws IOException {
		this.lookahead = this.input.nextToken();
	}

	// === Error messages ===

	private String getErrorMsg(TokenType fstAlternative, TokenType... alternatives) {
		StringBuilder buffer = new StringBuilder(UNEXPECTED_TOKEN);
		buffer.append(this.lookahead);
		buffer.append(AT_LINE);
		buffer.append(this.input.getLine());
		buffer.append(".");
		buffer.append(EXPECTED);
		buffer.append(fstAlternative);
		for (int i = 0; i < alternatives.length; i++) {
			buffer.append(OR);
			buffer.append(alternatives[i]);
		}
		buffer.append(".");
		return buffer.toString();
	}
}
