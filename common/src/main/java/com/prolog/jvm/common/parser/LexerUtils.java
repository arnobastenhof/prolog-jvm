package com.prolog.jvm.common.parser;

/**
 * Utility class containing common constants and functions for use by
 * {@link Lexer}s.
 *
 * @author Arno Bastenhof
 *
 */
public final class LexerUtils {

	/**
	 * The end-of-file character.
	 */
	public static final char EOF = (char)-1;

	// Private constructor to prevent instantiation.
	private LexerUtils() {}

	/**
	 * Returns true iff the supplied character is a digit. In EBNF:
	 * <pre>
	 * digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
	 * </pre>
	 */
	public static boolean isDigit(char c) {
		return 47 < c && c < 58;
	}

	/**
	 * Returns true iff the supplied character is a capital letter. In EBNF:
	 * <pre>
	 * capital = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J"
	 *         | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T"
	 *         | "U" | "V" | "W" | "X" | "Y" | "Z" ;
	 * </pre>
	 */
	public static boolean isCapitalLetter(char c) {
		return 64 < c && c < 91;
	}

	/**
	 * Returns true iff the supplied character is a small letter. In EBNF:
	 * <pre>
	 * small = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j"
	 *       | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t"
	 *       | "u" | "v" | "w" | "x" | "y" | "z" ;
	 * </pre>
	 */
	public static boolean isSmallLetter(char c) {
		return 96 < c && c < 123;
	}

	/**
	 * Returns true iff the supplied character is a tab, a linefeed, a carriage
	 * return or a single space. In EBNF:
	 * <pre>
	 * whitespace = "\t" | "\n" | "\r" | " " ;
	 * </pre>
	 */
	public static boolean isWhitespace(char c) {
		return c == '\t' || c == '\n' || c == '\r' || c == ' ';
	}

	/**
	 * Returns true iff the supplied character can be part of a graphic token,
	 * as defined by the proposed ISO standard for PROLOG. In EBNF:
	 * <pre>
	 * graphic = "#" | "$" | "&" | "*" | "+" | "-" | "." | "/" | ":" | "<"
	 *         | "=" | ">" | "?" | "@" | "^" | "~" | "\" ;
	 * </pre>
	 */
	public static boolean isGraphic(char c) {
		return c == '#' || c == '$' || c == '&' || c == '*'
				|| c == '+' || c == '-' || c == '.' || c == '/'
				|| c == ':' || c == '<' || c == '=' || c == '>'
				|| c == '?' || c == '@' || c == '^' || c == '~'
				|| c == '\\';
	}
}
