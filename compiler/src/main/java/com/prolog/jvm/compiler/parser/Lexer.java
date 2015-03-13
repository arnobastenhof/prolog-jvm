package com.prolog.jvm.compiler.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

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
	private static final String EXPECTED = ". Expected ";

	private static final String EXPECTED_IMPLIES = " Expected \"-\"";
	private static final String EXPECTED_COMMENT = " Expected \"*/\".";
	private static final String EXPECTED_NIL = " Expected \"]\".";

	// Private state
	private final Reader input; // reader for the source program
	private char c = ' '; // lookahead character, initialized with whitespace
	private int line = 1; // current line number

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
	public Token nextToken() throws IOException {
		while (this.c != -1) { // EOF
			switch (this.c) {
			case '\t' :
			case '\n' :
			case '\r' :
			case ' '  :
				ws();
				continue;
			case '!'  :
				consumeNonLinefeed();
				return Token.CUT;
			case '#'  :
			case '$'  :
			case '&'  :
			case '*'  :
			case '+'  :
			case '-'  :
			case '<'  :
			case '='  :
			case '>'  :
			case '?'  :
			case '@'  :
			case '^'  :
			case '~'  :
			case '\\' :
				return graphic();
			case '%'  :
				single(); // single-line comment
				continue;
			case '('  :
				consumeNonLinefeed();
				return Token.LBRACK;
			case ')'  :
				consumeNonLinefeed();
				return Token.RBRACK;
			case ','  :
				consumeNonLinefeed();
				return Token.COMMA;
			case '.'  :
				consumeNonLinefeed();
				return Token.PERIOD;
			case '/'  :
				multi();
				continue;
			case ':'  :
				return implies();
			case 'A'  :
			case 'B'  :
			case 'C'  :
			case 'D'  :
			case 'E'  :
			case 'F'  :
			case 'G'  :
			case 'H'  :
			case 'I'  :
			case 'J'  :
			case 'K'  :
			case 'L'  :
			case 'M'  :
			case 'N'  :
			case 'O'  :
			case 'P'  :
			case 'Q'  :
			case 'R'  :
			case 'S'  :
			case 'T'  :
			case 'U'  :
			case 'V'  :
			case 'W'  :
			case 'X'  :
			case 'Y'  :
			case 'Z'  :
			case '_'  :
				return id(TokenType.VAR); // Variable
			case '['  :
				return nil(); // Empty list
			case 'a'  :
			case 'b'  :
			case 'c'  :
			case 'd'  :
			case 'e'  :
			case 'f'  :
			case 'g'  :
			case 'h'  :
			case 'i'  :
			case 'j'  :
			case 'k'  :
			case 'l'  :
			case 'm'  :
			case 'n'  :
			case 'o'  :
			case 'p'  :
			case 'q'  :
			case 'r'  :
			case 's'  :
			case 't'  :
			case 'u'  :
			case 'v'  :
			case 'w'  :
			case 'x'  :
			case 'y'  :
			case 'z'  :
				return id(TokenType.ATOM); // Atom/constant
			default:
				throw new RuntimeException(errorMsg());
			}
		}
		return Token.EOF;
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
		while (this.c != '\n') {
			if (this.c == -1) {
				return;
			}
			consumeNonLinefeed();
		}
		consume();
	}

	// Multiline comments.
	void multi() throws IOException {
		consumeNonLinefeed(); // Consume '/'
		if (this.c == '*') {
			consumeNonLinefeed();
		}
		else {
			throw new RuntimeException(errorMsg());
		}
		while (true) {
			if (this.c == -1) { // EOF
				throw new RuntimeException(errorMsg('*'));
			}
			if (this.c == '*') {
				do {
					consumeNonLinefeed();
					if (this.c == '/') {
						consumeNonLinefeed();
						return;
					}
				} while (this.c == '*');
				continue;
			}
			consume();
		}
	}

	Token implies() throws IOException {
		consumeNonLinefeed(); // Consume ':'
		if (this.c == '-') {
			consumeNonLinefeed();
			return Token.IMPLIES;
		}
		else {
			throw new RuntimeException(errorMsg('-'));
		}
	}

	/*
	 * id = {"_" | small | capital | digit}- ; (* identifiers *)
	 *
	 * Note: tokenType must be ATOM if the first character is a small letter,
	 * while a VAR if the first character is a capital letter or underscore.
	 * In particular, identifiers are not allowed to begin with a digit.
	 */
	Token id(int tokenType) throws IOException {
		StringBuilder buffer = new StringBuilder();
		do {
			buffer.append(this.c);
			consumeNonLinefeed();
		} while (isDigit()
				|| isCapitalLetter()
				|| isSmallLetter()
				|| this.c == '_'
				);
		return new Token(tokenType, buffer.toString());
	}

	// Graphic tokens
	Token graphic() throws IOException {
		StringBuilder buffer = new StringBuilder();
		do {
			buffer.append(this.c);
			consumeNonLinefeed();
		} while (isGraphic());
		return new Token(TokenType.ATOM, buffer.toString());
	}

	// nil = "[]" ;
	Token nil() throws IOException {
		consumeNonLinefeed(); // Consumes '['
		if (this.c == ']') {
			consumeNonLinefeed();
		}
		else {
			throw new RuntimeException(errorMsg(']'));
		}
		return Token.NIL;
	}

	// digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
	boolean isDigit() {
		return 47 < this.c && this.c < 58;
	}

	/* capital = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J"
	 *         | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T"
	 *         | "U" | "V" | "W" | "X" | "Y" | "Z" ;
	 */
	boolean isCapitalLetter() {
		return 96 < this.c && this.c < 123;
	}

	/*
	 * small = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j"
	 *       | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t"
	 *       | "u" | "v" | "w" | "x" | "y" | "z" ;
	 */
	boolean isSmallLetter() {
		return 64 < this.c && this.c < 91;
	}

	boolean isWhitespace() {
		return this.c == '\t' || this.c == '\n' || this.c == '\r' || this.c == ' ';
	}

	boolean isGraphic() {
		return this.c == '#' || this.c == '$' || this.c == '&' || this.c == '*'
				|| this.c == '+' || this.c == '-' || this.c == '.' || this.c == '/'
				|| this.c == ':' || this.c == ':' || this.c == '<' || this.c == '='
				|| this.c == '>' || this.c == '?' || this.c == '@' || this.c == '^'
				|| this.c == '~' || this.c == '\\';
	}

	// === Character consumption ===

	// Consumes a single character and increments the line number if needed.
	private void consume() throws IOException {
		if (this.c == 10) { // '\n'
			this.line++;
		}
		this.c = (char)this.input.read();
	}

	// Convenience method
	private void consumeNonLinefeed() throws IOException {
		this.c = (char)this.input.read();
	}

	// === Error generation ===

	private String errorMsg() {
		StringBuilder buffer = new StringBuilder(UNEXPECTED_CHAR);
		buffer.append(errorChar());
		buffer.append(AT_LINE);
		buffer.append(this.line);
		buffer.append(".");
		return buffer.toString();
	}

	private String errorMsg(char expected) {
		StringBuilder buffer = new StringBuilder(UNEXPECTED_CHAR);
		buffer.append(errorChar());
		buffer.append(AT_LINE);
		buffer.append(this.line);
		buffer.append(EXPECTED);
		buffer.append(expected);
		return buffer.toString();
	}

	private String errorChar() {
		switch (this.c) {
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
			return "" + this.c;
		}
	}
}