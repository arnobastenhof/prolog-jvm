package com.prolog.jvm.compiler.parser;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;

import com.prolog.jvm.exceptions.RecognitionException;

/**
 * A tokenizer for a subset of Prolog, based on Covington (1993), ISO Prolog: A
 * Summary of the Draft Proposed Standard.
 *
 * The following simplifications were made compared to the proposed ISO
 * standard:
 * <ul>
 * <li>Graphic tokens are not allowed to begin with {@code .}, {@code /} or
 * {@code :}. This ensures we can make do with a single lookahead character. To
 * compare, the proposed ISO standard only prohibited graphic tokens from
 * beginning with {@code /*}.
 * <li>No support for <code>{}</code> as an atom.
 * <li>No support for arbitrary characters inside single quotes as an atom.
 * <li>No support for numbers or character strings.
 * <li>No reserved identifiers.
 * <li>Whitespace is always ignored. In particular, we do not prohibit it from
 * occurring between a functor and its opening bracket, nor do we demand a
 * period be followed by it.
 * </ul>
 *
 * @author Arno Bastenhof
 */
public final class PrologLexer extends AbstractLexer {

    /**
     * Static factory method for obtaining a new {@link Lexer} instance reading
     * from the specified {@code input}.
     *
     * @param input a {@link Reader} for the input; not allowed to be null
     * @throws NullPointerException if {@code input == null}
     * @throws IOException
     */
    public static PrologLexer newInstance(final Reader input) {
        return new PrologLexer(requireNonNull(input));
    }

    // Package-private constructor used by static factory method
    PrologLexer(final Reader input) {
        super(input);
    }

    /**
     * Invoked by the parser to request a new token from the input.
     *
     * @return The next token read from the input.
     * @throws IOException
     */
    @Override
    public Token nextToken() throws IOException, RecognitionException {
        while (getLookahead() != EOF) {
            if (isWhitespace()) {
                ws();
                continue;
            }
            if (isCapitalLetter() || isSmallLetter() || getLookahead() == '_') {
                return id();
            }
            switch (getLookahead()) {
            case '%':
                single(); // single-line comment
                continue;
            case '#':
            case '$':
            case '&':
            case '*':
            case '+':
            case '-':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case '^':
            case '~':
            case '\\':
                return graphic();
            case '(':
                consumeNonLinefeed();
                return Tokens.LBRACK;
            case ')':
                consumeNonLinefeed();
                return Tokens.RBRACK;
            case ',':
                consumeNonLinefeed();
                return Tokens.COMMA;
            case '.':
                consumeNonLinefeed();
                return Tokens.PERIOD;
            case '/':
                multi();
                continue;
            case ':':
                return implies();
            case '[':
                return nil(); // Empty list
            default:
                throw RecognitionException.newInstance(getLookahead(),
                        getLine());
            }
        }
        return Tokens.EOF;
    }

    // === Lexer rules (Package private for testing purposes) ===
    // Comments reference the ISO standard for Extended Backus-Naur Form.

    // ws = {"\t" | "\n" | "\r" | " "}- ; (* whitespace *)
    private void ws() throws IOException {
        do {
            consume();
        } while (isWhitespace());
    }

    // Inline comments.
    private void single() throws IOException {
        consumeNonLinefeed(); // Consume '%'
        while (getLookahead() != '\n') {
            if (getLookahead() == -1) {
                return;
            }
            consumeNonLinefeed();
        }
        consume();
    }

    // Multiline comments.
    private void multi() throws IOException, RecognitionException {
        consumeNonLinefeed(); // Consume '/'
        match('*');
        while (true) {
            if (getLookahead() == EOF) {
                throw RecognitionException.newInstance(getLookahead(),
                        getLine(), new String[] { "*" });
            }
            if (getLookahead() == '*') {
                do {
                    consumeNonLinefeed();
                    if (getLookahead() == '/') {
                        consumeNonLinefeed();
                        return;
                    }
                } while (getLookahead() == '*');
                continue;
            }
            consume();
        }
    }

    private Token implies() throws IOException, RecognitionException {
        consumeNonLinefeed(); // Consume ':'
        match('-');
        return Tokens.IMPLIES;
    }

    /*
     * id = {"_" | small | capital | digit}- ; (* identifiers *)
     *
     * Note: tokenType must be ATOM if the first character is a small letter,
     * while a VAR if the first character is a capital letter or underscore. In
     * particular, identifiers are not allowed to begin with a digit.
     */
    private Token id() throws IOException {
        boolean smallLetter = isSmallLetter();
        final StringBuilder buffer = new StringBuilder();
        do {
            buffer.append(getLookahead());
            consumeNonLinefeed();
        } while (isDigit() || isCapitalLetter() || isSmallLetter()
                || getLookahead() == '_');
        return smallLetter ? Tokens.getAtom(buffer.toString()) : Tokens
                .getVar(buffer.toString());
    }

    // Graphic tokens
    private Token graphic() throws IOException {
        final StringBuilder buffer = new StringBuilder();
        do {
            buffer.append(getLookahead());
            consumeNonLinefeed();
        } while (isGraphic());
        return Tokens.getAtom(buffer.toString());
    }

    // nil = "[]" ;
    private Token nil() throws IOException, RecognitionException {
        consumeNonLinefeed(); // Consumes '['
        match(']');
        return Tokens.NIL;
    }

    /*
     * Returns true iff the supplied character is a digit. In EBNF:
     *
     * digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
     */
    private boolean isDigit() {
        return 47 < getLookahead() && getLookahead() < 58;
    }

    /*
     * Returns true iff the supplied character is a capital letter. In EBNF:
     *
     * capital = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K"
     * | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" |
     * "X" | "Y" | "Z" ;
     */
    private boolean isCapitalLetter() {
        return 64 < getLookahead() && getLookahead() < 91;
    }

    /*
     * Returns true iff the supplied character is a small letter. In EBNF:
     *
     * small = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" |
     * "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" |
     * "x" | "y" | "z" ;
     */
    private boolean isSmallLetter() {
        return 96 < getLookahead() && getLookahead() < 123;
    }

    /*
     * Returns true iff the supplied character is a tab, a linefeed, a carriage
     * return or a single space. In EBNF:
     *
     * whitespace = "\t" | "\n" | "\r" | " " ;
     */
    private boolean isWhitespace() {
        return getLookahead() == '\t' || getLookahead() == '\n'
                || getLookahead() == '\r' || getLookahead() == ' ';
    }

    /*
     * Returns true iff the supplied character can be part of a graphic token,
     * as defined by the proposed ISO standard for PROLOG. In EBNF:
     *
     * graphic = "#" | "$" | "&" | "*" | "+" | "-" | "." | "/" | ":" | "<" | "="
     * | ">" | "?" | "@" | "^" | "~" | "\" ;
     */
    private boolean isGraphic() {
        return getLookahead() == '#' || getLookahead() == '$'
                || getLookahead() == '&' || getLookahead() == '*'
                || getLookahead() == '+' || getLookahead() == '-'
                || getLookahead() == '.' || getLookahead() == '/'
                || getLookahead() == ':' || getLookahead() == '<'
                || getLookahead() == '=' || getLookahead() == '>'
                || getLookahead() == '?' || getLookahead() == '@'
                || getLookahead() == '^' || getLookahead() == '~'
                || getLookahead() == '\\';
    }
}