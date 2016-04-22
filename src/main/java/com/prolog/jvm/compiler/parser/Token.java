package com.prolog.jvm.compiler.parser;

/**
 * The type for Tokens, used by {@link Lexer}s for categorizing the input text.
 *
 * @author Arno Bastenhof
 */
public interface Token {

    /**
     * Returns this token's type.
     */
    TokenType getType();

    /**
     * Returns the matched input text.
     */
    String getText();

}
