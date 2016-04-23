package com.prolog.jvm.exceptions;

import com.prolog.jvm.compiler.parser.Token;

/**
 * The type of exception to use upon encountering an unexpected lexeme during
 * tokenization or parsing.
 *
 * @author Arno Bastenhof
 *
 */
public final class RecognitionException extends Exception {

    private static final long serialVersionUID = -6218963714733666401L;

    // Strings used to compose error messages.
    private static final String UNEXPECTED_AT_LINE = " unexpected at line ";
    private static final String EXPECTED = " Expected ";
    private static final String OR = ", or ";

    /**
     * Returns a new instance for reporting tokenization exceptions.
     *
     * @param actual the encountered character
     * @param line the line number at which {@code actual} was encountered
     * @param expected the String representations for the expected character(s);
     * can be empty or null
     */
    public static RecognitionException newInstance(final char actual,
            final int line, final String[] expected) {
        return new RecognitionException(getErrorMsg(getErrorChar(actual), line,
                expected));
    }

    /**
     * Returns a new instance for reporting tokenization exceptions.
     *
     * @param actual the encountered character
     * @param line the line number at which {@code actual} was encountered
     */
    public static RecognitionException newInstance(final char actual,
            final int line) {
        return newInstance(actual, line, null);
    }

    /**
     * Returns a new instance for reporting parsing exceptions.
     *
     * @param actual the encountered token type
     * @param line the line number at which {@code actual} was encountered
     * @param expected the String representations for the expected token
     * type(s); can be empty or null
     */
    public static RecognitionException newInstance(final Token actual,
            final int line, final String[] expected) {
        return new RecognitionException(getErrorMsg(actual.toString(), line,
                expected));
    }

    /**
     * Returns a new instance for reporting parsing exceptions.
     *
     * @param actual the encountered token type
     * @param line the line number at which {@code actual} was encountered
     */
    public static RecognitionException newInstance(final Token actual,
            final int line) {
        return new RecognitionException(getErrorMsg(actual.toString(), line,
                null));
    }

    // Private constructor
    private RecognitionException(final String msg) {
        super(msg);
    }

    private static String getErrorMsg(final String actual, final int line,
            final String[] expected) {
        assert actual != null;
        assert line > 0;
        final StringBuilder buffer = new StringBuilder(actual)
                .append(UNEXPECTED_AT_LINE).append(line).append(".");
        if (expected != null && expected.length > 0) {
            buffer.append(EXPECTED).append(expected[0]);
            for (int i = 1; i < expected.length; i++) {
                buffer.append(OR).append(expected[i]);
            }
            buffer.append(".");
        }
        return buffer.toString();
    }

    private static String getErrorChar(final char actual) {
        switch (actual) {
        case (char) -1: // EOF
            return "<EOF>";
        case '\t': // '\t'
            return "\\t";
        case '\n': // '\n'
            return "\\n";
        case '\r': // '\r'
            return "\\r";
        case ' ': // ' '
            return "' '";
        default:
            return "" + actual;
        }
    }
}
