package com.prolog.jvm.compiler.parser;

import java.util.Objects;

/**
 * This class defines the tokens produced from a source program by the {@link Lexer}.
 * A token's state consists of a token type, together with the matched input text.
 *
 * @author Arno Bastenhof
 *
 */
public final class Token {

	// === Constants and static factory methods for obtaining Tokens ===

	/**
	 * The <code>Token</code> indicating the end of the source file.
	 */
	public static final Token EOF = new Token(TokenType.EOF, "<EOF>");

	/**
	 * The <code>Token</code> corresponding to occurrences of Cut.
	 */
	public static final Token CUT = new Token(TokenType.CUT, "!");

	/**
	 * The <code>Token</code> corresponding to occurrences of the empty list.
	 */
	public static final Token NIL = new Token(TokenType.NIL, "[]");

	/**
	 * The <code>Token</code> corresponding to occurrences of the implication sign.
	 */
	public static final Token IMPLIES = new Token(TokenType.IMPLIES, ":-");

	/**
	 * The <code>Token</code> corresponding to occurrences of a comma in the
	 * source program.
	 */
	public static final Token COMMA = new Token(TokenType.COMMA, ",");

	/**
	 * The <code>Token</code> corresponding to occurrences of a period in the
	 * source program.
	 */
	public static final Token PERIOD = new Token(TokenType.PERIOD, ".");

	/**
	 * The <code>Token</code> corresponding to occurrences of a left bracket
	 * in the source program.
	 */
	public static final Token LBRACK = new Token(TokenType.LBRACK, "(");

	/**
	 * The <code>Token</code> corresponding to occurrences of a right bracket
	 * in the source program.
	 */
	public static final Token RBRACK = new Token(TokenType.RBRACK, ")");

	/**
	 * Static factory method for obtaining a <code>Token</code> of type
	 * <code>ATOM</code>.
	 *
	 * @param text The matched input text.
	 */
	public static final Token getAtom(String text) {
		return new Token(TokenType.ATOM, text);
	}

	/**
	 * Static factory method for obtaining a <code>Token</code> of type
	 * <code>VAR</code>.
	 *
	 * @param text The matched input text.
	 */
	public static final Token getVar(String text) {
		return new Token(TokenType.VAR, text);
	}

	// === Private state ===

	private final TokenType type;
	private final String text;

	// === Constructors and public methods ===

	// Private constructor to force instantiation through static factory methods.
	private Token(TokenType type, String text) {
		this.type = type;
		this.text = text;
	}

	/**
	 * Returns this token's type.
	 */
	public final TokenType getType() {
		return this.type;
	}

	/**
	 * Returns the text from the source program corresponding to this token.
	 */
	public final String getText() {
		return this.text;
	}

	/**
	 * Returns the String representation of this token, consisting of the
	 * matched text together with the token type's name, separated by a
	 * semicolon and delimited by angular brackets. E.g., &lt;,;COMMA>,
	 * &lt;append;ATOM>, etc.
	 */
	@Override
	public final String toString() {
		return "<" + this.text + ";" + this.type + ">";
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.type, this.text);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public final boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Token)) {
			return false;
		}
		Token other = (Token)obj;
		return this.type == other.type
				&& (this.text == null ?
						other.text == null : this.text.equals(other.text));
	}
}
