package com.prolog.jvm.common.exceptions;

import com.prolog.jvm.common.parser.Token;

/**
 * The type of exception to use upon encountering an unexpected lexeme during
 * tokenization or parsing.
 *
 * @author Arno Bastenhof
 *
 */
public final class RecognitionException extends RuntimeException {

	private static final long serialVersionUID = -6218963714733666401L;

	// Strings used to compose error messages.
	private static final String UNEXPECTED_AT_LINE = " unexpected at line ";
	private static final String EXPECTED = " Expected ";
	private static final String OR = ", or ";

	/**
	 * Returns a new instance for reporting tokenization exceptions.
	 *
	 * @param actual The encountered character.
	 * @param line The line number at which <code>actual</code> was encountered.
	 * @param expected The String representations for the expected character(s).
	 * Can be empty or null.
	 */
	public static <T> RecognitionException newInstance(char actual, int line, String[] expected) {
		return new RecognitionException(getErrorMsg(getErrorChar(actual), line, expected));
	}

	/**
	 * Returns a new instance for reporting tokenization exceptions.
	 *
	 * @param actual The encountered character.
	 * @param line The line number at which <code>actual</code> was encountered.
	 */
	public static <T> RecognitionException newInstance(char actual, int line) {
		return newInstance(actual, line, null);
	}

	/**
	 * Returns a new instance for reporting parsing exceptions.
	 *
	 * @param actual The encountered token type.
	 * @param line The line number at which <code>actual</code> was encountered.
	 * @param expected The String representations for the expected token type(s).
	 * Can be empty or null.
	 */
	public static <T> RecognitionException newInstance(Token<T> actual, int line, String[] expected) {
		return new RecognitionException(getErrorMsg(actual.toString(), line, expected));
	}

	/**
	 * Returns a new instance for reporting parsing exceptions.
	 *
	 * @param actual The encountered token type.
	 * @param line The line number at which <code>actual</code> was encountered.
	 */
	public static <T> RecognitionException newInstance(Token<T> actual, int line) {
		return new RecognitionException(getErrorMsg(actual.toString(), line, null));
	}

	// Private constructor
	private RecognitionException(String msg) {
		super(msg);
	}

	private static String getErrorMsg(String actual, int line, String[] expected) {
		StringBuilder buffer = new StringBuilder(actual);
		buffer.append(UNEXPECTED_AT_LINE);
		buffer.append(line);
		buffer.append(".");
		if (expected != null && expected.length > 0) {
			buffer.append(EXPECTED);
			buffer.append(expected[0]);
			for (int i = 1; i < expected.length; i++) {
				buffer.append(OR);
				buffer.append(expected[i]);
			}
			buffer.append(".");
		}
		return buffer.toString();
	}

	private static String getErrorChar(char actual) {
		switch (actual) {
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
			return "" + actual;
		}
	}
}
