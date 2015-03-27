package com.prolog.jvm.common.parser;

import java.io.IOException;
import java.io.Reader;

/**
 * Skeletal implementation for a lexical analyzer based on Pattern 2, LL(1)
 * Recursive-Descent Lexer from Parr (2010), Language Implementation Patterns.
 *
 * @author Arno Bastenhof
 *
 */
public abstract class AbstractLexer<T> implements Lexer<T> {

	// Strings used to compose error messages.
	private static final String UNEXPECTED_CHAR = "Unexpected character ";
	private static final String AT_LINE = " at line ";
	private static final String EXPECTED = " Expected ";

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
	 * @param c The character to be matched against.
	 * @throws IOException
	 */
	protected void match(char c) throws IOException {
		if (this.lookahead == c) {
			consumeNonLinefeed();
		}
		else {
			throw new RuntimeException(getErrorMsg(c));
		}
	}

	// === Error messages ===

	/**
	 * Returns an error message to the extent that the occurrence of the
	 * lookahead character at the line number currently being processed was
	 * unexpected.
	 */
	protected String getErrorMsg() {
		return getErrorMsgPrefix().toString();
	}

	/**
	 * Returns an error message to the extent that the occurrence of the
	 * lookahead character at the line number currently being processed was
	 * unexpected, having rather expected the character supplied as a parameter.
	 *
	 * @param expected The expected character.
	 */
	protected String getErrorMsg(char expected) {
		StringBuilder buffer = getErrorMsgPrefix();
		buffer.append(EXPECTED);
		buffer.append(expected);
		buffer.append(".");
		return buffer.toString();
	}

	private StringBuilder getErrorMsgPrefix() {
		StringBuilder buffer = new StringBuilder(UNEXPECTED_CHAR);
		buffer.append(getErrorChar());
		buffer.append(AT_LINE);
		buffer.append(this.line);
		buffer.append(".");
		return buffer;
	}

	private String getErrorChar() {
		switch (this.lookahead) {
		case (char) -1  : // EOF
			return "<EOF>";
		case '\t' : // '\t'
			return "\\t";
		case '\n' : // '\n'
			return "\\n";
		case '\r' : // '\r'
			return "\\r";
		case ' '  : // ' '
			return "' '";
		default:
			return "" + this.lookahead;
		}
	}
}
