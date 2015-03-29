package com.prolog.jvm.common.parser;

import java.io.IOException;
import java.io.Reader;

import com.prolog.jvm.common.exceptions.RecognitionException;

/**
 * Skeletal implementation for a lexical analyzer based on Pattern 2, LL(1)
 * Recursive-Descent Lexer from Parr (2010), Language Implementation Patterns.
 *
 * @author Arno Bastenhof
 *
 */
public abstract class AbstractLexer<T> implements Lexer<T> {

	// Private state
	private final Reader input; // reader for the input text
	private char lookahead = ' '; // lookahead character, initialized with whitespace
	private int line = 1; // current line number

	/**
	 * Constructs a <code>Lexer</code> instance reading from a
	 * <code>Reader</code> and initializes the lookahead character.
	 */
	protected AbstractLexer(Reader input) {
		this.input = input;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public final int getLine() {
		return this.line;
	}

	/**
	 * Returns the lookahead character.
	 */
	protected final char getLookahead() {
		return this.lookahead;
	}

	// === Character consumption ===

	/**
	 * Consumes a single character and increments the line number if needed.
	 *
	 * @throws IOException
	 */
	protected void consume() throws IOException {
		if (this.lookahead == '\n') {
			this.line++;
		}
		this.lookahead = (char)this.input.read();
	}

	/**
	 * Convenience method serving as an alternative to {@link #consume()} in case
	 * it is known that the lookahead character is not a linefeed.
	 *
	 * @throws IOException
	 */
	protected void consumeNonLinefeed() throws IOException {
		this.lookahead = (char)this.input.read();
	}

	/**
	 * Compares the lookahead character against the supplied parameter, throwing
	 * an exception in case of a mismatch.
	 *
	 * @param expected The expected character.
	 * @throws IOException
	 */
	protected void match(char expected) throws IOException {
		if (this.lookahead == expected) {
			consumeNonLinefeed();
		}
		else {
			throw RecognitionException.newInstance(
					this.lookahead,
					this.line,
					new String[]{String.valueOf(expected)});
		}
	}
}
