package com.prolog.jvm.compiler.parser;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import com.prolog.jvm.exceptions.RecognitionException;

/**
 * Skeletal implementation for a parser based on Pattern 3 of Parr (2010),
 * Language Implementation Patterns: LL(1) Recursive-Descent Parser.
 *
 * @author Arno Bastenhof
 */
public abstract class AbstractParser {

	private final Lexer input;
	private Token lookahead;

	/**
	 *
	 * @param input a lexer instance for the Prolog source program or query;
	 * not allowed to be null
	 * @throws NullPointerException if {@code input == null}
	 */
	protected AbstractParser(final Lexer input) {
		this.input = requireNonNull(input);
	}

	// === Token consumption ===

	/**
	 * Returns the type of the current lookahead token.
	 */
	protected final TokenType getLookaheadType() {
		return this.lookahead.getType();
	}

	/**
	 * Matches the current lookahead token against the supplied type, throwing an
	 * exception in case of a mismatch.
	 *
	 * @param expected the expected token type; not allowed to be null
	 * @throws NullPointerException if {@code expected == null}
	 * @throws RecognitionException if {@link #getLookaheadType()} does not
	 * coincide with {@code expected}
	 * @throws IOException
	 */
	protected final void match(final TokenType expected)
			throws IOException, RecognitionException {
		requireNonNull(expected);
		if (this.lookahead.getType() == expected) {
			consume();
		}
		else {
			throw RecognitionException.newInstance(
					this.lookahead,
					this.input.getLine(),
					new String[]{expected.toString()});
		}
	}

	/**
	 * Consumes the current lookahead token.
	 *
	 * @throws IOException
	 * @throws RecognitionException
	 */
	protected final void consume() throws IOException, RecognitionException {
		this.lookahead = this.input.nextToken();
	}

	// === Error messages ===

	/**
	 * Returns the current lookahead token.
	 */
	protected final Token getLookahead() {
		return this.lookahead;
	}

	/**
	 * Returns the line number currently being proccessed.
	 */
	protected final int getLine() {
		return this.input.getLine();
	}

}
