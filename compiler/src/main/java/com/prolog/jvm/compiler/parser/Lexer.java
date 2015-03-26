package com.prolog.jvm.compiler.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.prolog.jvm.common.parser.Token;

/**
 * A tokenizer for a subset of Prolog, based on Covington (1993), ISO Prolog:
 * A Summary of the Draft Proposed Standard. The implementation of this class
 * follows Pattern 2, LL(1) Recursive-Descent Lexer from Parr (2010), Language
 * Implementation Patterns.
 *
 * The following simplifications were made compared to the proposed ISO standard:
 * <ul>
 * <li> Graphic tokens are not allowed to begin with <code>.</code>, <code>/</code>
 * or <code>:</code>. This ensures we can make do with a single lookahead character.
 * To compare, the proposed ISO standard only prohibited graphic tokens from
 * beginning with <code>/*</code>.
 * <li> No support for <code>{}</code> as an atom.
 * <li> No support for arbitrary characters inside single quotes as an atom.
 * <li> No support for numbers or character strings.
 * <li> No reserved identifiers.
 * <li> Whitespace is always ignored. In particular, we do not prohibit it from
 * occurring between a functor and its opening bracket, nor do we demand a period
 * be followed by it.
 * </ul>
 *
 * @author Arno Bastenhof
 *
 */
public final class Lexer {

	// Strings used to compose error messages.
	private static final String UNEXPECTED_CHAR = "Unexpected character ";
	private static final String AT_LINE = " at line ";
	private static final String EXPECTED = " Expected ";

	// Private state
	private final Reader input; // reader for the source program
	private char lookahead = ' '; // lookahead character, initialized with whitespace
	private int line = 1; // current line number

	int getLine() {
		return this.line;
	}

	// Package-private constructor used by static factory methods for the Parser
	Lexer(Reader input) {
		this.input = input;
	}

	/**
	 * Constructs a <code>Lexer</code> instance reading from a
	 * <code>String</code> and initializes the lookahead character.
	 * @throws IOException
	 */
	public Lexer(String input) {
		this.input = new StringReader(input);
	}

	/**
	 * Constructs a <code>Lexer</code> instance reading from an
	 * {@link InputStream} and initializes the lookahead character.
	 * @throws IOException
	 */
	public Lexer(InputStream input) {
		this.input = new InputStreamReader(input);
	}

	/**
	 * Invoked by the parser to request a new token from the input.
	 *
	 * @return The next token read from the input.
	 * @throws IOException
	 */
	public Token<PlTokenType> nextToken() throws IOException {
		while (this.lookahead != (char)-1) { // EOF
			if (isWhitespace()) {
				ws();
				continue;
			}
			if (isCapitalLetter() || isSmallLetter() || this.lookahead == '_') {
				return id();
			}
			switch (this.lookahead) {
			case '!'  :
				consumeNonLinefeed();
				return PrologTokens.CUT;
			case '%'  :
				single(); // single-line comment
				continue;
			case '#' :
			case '$' :
			case '&' :
			case '*' :
			case '+' :
			case '-' :
			case '<' :
			case '=' :
			case '>' :
			case '?' :
			case '@' :
			case '^' :
			case '~' :
			case '\\' :
				return graphic();
			case '('  :
				consumeNonLinefeed();
				return PrologTokens.LBRACK;
			case ')'  :
				consumeNonLinefeed();
				return PrologTokens.RBRACK;
			case ','  :
				consumeNonLinefeed();
				return PrologTokens.COMMA;
			case '.'  :
				consumeNonLinefeed();
				return PrologTokens.PERIOD;
			case '/'  :
				multi();
				continue;
			case ':'  :
				return implies();
			case '['  :
				return nil(); // Empty list
			default:
				throw new RuntimeException(getErrorMsg());
			}
		}
		return PrologTokens.EOF;
	}

	// === Lexer rules (Package private for testing purposes) ===
	// Comments reference the ISO standard for Extended Backus-Naur Form.

	// ws = {"\t" | "\n" | "\r" | " "}- ; (* whitespace *)
	void ws() throws IOException {
		do {
			consume();
		} while (isWhitespace());
	}

	// Inline comments.
	void single() throws IOException {
		consumeNonLinefeed(); // Consume '%'
		while (this.lookahead != '\n') {
			if (this.lookahead == -1) {
				return;
			}
			consumeNonLinefeed();
		}
		consume();
	}

	// Multiline comments.
	void multi() throws IOException {
		consumeNonLinefeed(); // Consume '/'
		match('*');
		while (true) {
			if (this.lookahead == -1) { // EOF
				throw new RuntimeException(getErrorMsg('*'));
			}
			if (this.lookahead == '*') {
				do {
					consumeNonLinefeed();
					if (this.lookahead == '/') {
						consumeNonLinefeed();
						return;
					}
				} while (this.lookahead == '*');
				continue;
			}
			consume();
		}
	}

	Token<PlTokenType> implies() throws IOException {
		consumeNonLinefeed(); // Consume ':'
		match('-');
		return PrologTokens.IMPLIES;
	}

	/*
	 * id = {"_" | small | capital | digit}- ; (* identifiers *)
	 *
	 * Note: tokenType must be ATOM if the first character is a small letter,
	 * while a VAR if the first character is a capital letter or underscore.
	 * In particular, identifiers are not allowed to begin with a digit.
	 */
	Token<PlTokenType> id() throws IOException {
		boolean smallLetter = isSmallLetter();
		StringBuilder buffer = new StringBuilder();
		do {
			buffer.append(this.lookahead);
			consumeNonLinefeed();
		} while (isDigit()
				|| isCapitalLetter()
				|| isSmallLetter()
				|| this.lookahead == '_'
				);
		return smallLetter ? PrologTokens.getAtom(buffer.toString())
				: PrologTokens.getVar(buffer.toString());
	}

	// Graphic tokens
	Token<PlTokenType> graphic() throws IOException {
		StringBuilder buffer = new StringBuilder();
		do {
			buffer.append(this.lookahead);
			consumeNonLinefeed();
		} while (isGraphic());
		return PrologTokens.getAtom(buffer.toString());
	}

	// nil = "[]" ;
	Token<PlTokenType> nil() throws IOException {
		consumeNonLinefeed(); // Consumes '['
		match(']');
		return PrologTokens.NIL;
	}

	// digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
	boolean isDigit() {
		return 47 < this.lookahead && this.lookahead < 58;
	}

	/* capital = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J"
	 *         | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T"
	 *         | "U" | "V" | "W" | "X" | "Y" | "Z" ;
	 */
	boolean isCapitalLetter() {
		return 64 < this.lookahead && this.lookahead < 91;
	}

	/*
	 * small = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j"
	 *       | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t"
	 *       | "u" | "v" | "w" | "x" | "y" | "z" ;
	 */
	boolean isSmallLetter() {
		return 96 < this.lookahead && this.lookahead < 123;
	}

	boolean isWhitespace() {
		return this.lookahead == '\t' || this.lookahead == '\n' || this.lookahead == '\r' || this.lookahead == ' ';
	}

	boolean isGraphic() {
		return this.lookahead == '#' || this.lookahead == '$' || this.lookahead == '&' || this.lookahead == '*'
				|| this.lookahead == '+' || this.lookahead == '-' || this.lookahead == '.' || this.lookahead == '/'
				|| this.lookahead == ':' || this.lookahead == ':' || this.lookahead == '<' || this.lookahead == '='
				|| this.lookahead == '>' || this.lookahead == '?' || this.lookahead == '@' || this.lookahead == '^'
				|| this.lookahead == '~' || this.lookahead == '\\';
	}

	// === Character consumption ===

	// Consumes a single character and increments the line number if needed.
	void consume() throws IOException {
		if (this.lookahead == 10) { // '\n'
			this.line++;
		}
		this.lookahead = (char)this.input.read();
	}

	// Convenience method
	private void consumeNonLinefeed() throws IOException {
		this.lookahead = (char)this.input.read();
	}

	// Tries to match the lookahead character to the parameter
	private void match(char c) throws IOException {
		if (this.lookahead == c) {
			consumeNonLinefeed();
		}
		else {
			throw new RuntimeException(getErrorMsg(c));
		}
	}

	// === Error messages ===

	private String getErrorMsg() {
		return getErrorMsgPrefix().toString();
	}

	private String getErrorMsg(char expected) {
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