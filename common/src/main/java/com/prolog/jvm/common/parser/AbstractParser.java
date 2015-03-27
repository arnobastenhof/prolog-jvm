package com.prolog.jvm.common.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Skeletal implementation for a parser based on Pattern 3 of Parr (2010), Language
 * Implementation Patterns: LL(1) Recursive-Descent Parser.
 *
 * @author Arno Bastenhof
 *
 * @param <T> The token type.
 */
public abstract class AbstractParser<T> {

	// Strings used to compose error messages.
	private static final String UNEXPECTED_TOKEN = "Unexpected token ";
	private static final String AT_LINE = " at line ";
	private static final String OR = ", or ";
	private static final String EXPECTED = " Expected type ";

	private final Lexer<T> input;
	private Token<T> lookahead;

	/**
	 *
	 * @param input A lexical analyzer.
	 */
	protected AbstractParser(Lexer<T> input) {
		this.input = input;
	}

	// === Token consumption ===

	/**
	 * Returns the type of the current lookahead token.
	 */
	protected T getLookaheadType() {
		return this.lookahead.getType();
	}

	/**
	 * Matches the current lookahead token against the supplied type, throwing an
	 * exception in case of a mismatch.
	 *
	 * @param type The expected token type.
	 * @throws IOException
	 */
	protected void match(T type) throws IOException {
		if (this.lookahead.getType() == type) {
			consume();
		}
		else {
			throw new RuntimeException(getErrorMsg(Collections.singletonList(type)));
		}
	}

	/**
	 * Consumes the current lookahead token.
	 *
	 * @throws IOException
	 */
	protected void consume() throws IOException {
		this.lookahead = this.input.nextToken();
	}

	// === Error messages ===

	/**
	 * Returns an error message to the extent that the current lookahead token was
	 * unexpected at the line currently being processed, optionally listing the
	 * expected token types.
	 *
	 * @param alternatives The list of expected token types. Can be empty or null.
	 */
	protected String getErrorMsg(List<T> alternatives) {
		StringBuilder buffer = new StringBuilder(UNEXPECTED_TOKEN);
		buffer.append(this.lookahead);
		buffer.append(AT_LINE);
		buffer.append(this.input.getLine());
		buffer.append(".");
		if (alternatives == null || !alternatives.isEmpty()) {
			buffer.append(EXPECTED);
			buffer.append(alternatives.get(0));
			for (int i = 1; i < alternatives.size(); i++) {
				buffer.append(OR);
				buffer.append(alternatives.get(i));
			}
			buffer.append(".");
		}
		return buffer.toString();
	}
}
