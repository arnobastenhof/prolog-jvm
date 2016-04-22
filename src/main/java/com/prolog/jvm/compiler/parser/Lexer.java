package com.prolog.jvm.compiler.parser;

import java.io.IOException;

import com.prolog.jvm.exceptions.RecognitionException;

/**
 * The type for lexical analyzers, used for converting input text into a
 * sequence of {@link Token}s.
 *
 * @author Arno Bastenhof
 */
public interface Lexer {

    /**
     * The end-of-file character.
     */
    public static final char EOF = (char) -1;

    /**
     * Invoked by a parser to request a new token from the input.
     *
     * @return The next token read from the input.
     * @throws IOException
     * @throws RecognitionException
     */
    Token nextToken() throws IOException, RecognitionException;

    /**
     * Returns the line number currently being processed.
     */
    int getLine();

}
