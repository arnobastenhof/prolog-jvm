package com.prolog.jvm.common.parser;

import java.io.IOException;

import com.prolog.jvm.common.exceptions.RecognitionException;

/**
 * Skeletal implementation for a parser based on Pattern 3 of Parr (2010), Language
 * Implementation Patterns: LL(1) Recursive-Descent Parser.
 *
 * @author Arno Bastenhof
 *
 * @param <T> The token type.
 */
public abstract class AbstractParser<T> {

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
			throw RecognitionException.newInstance(
					this.lookahead,
					this.input.getLine(),
					new String[]{type.toString()});
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
	 * Returns the current lookahead token.
	 */
	protected Token<T> getLookahead() {
		return this.lookahead;
	}

	/**
	 * Returns the line number currently being proccessed.
	 */
	protected int getLine() {
		return this.input.getLine();
	}


}
