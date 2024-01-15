package org.yapb4json.wc.yapb4j;

/**
 * What is a <b>lexeme</b> anyway? Taken from the Oxford Dictionary - it's a basic
 * lexical unit of a language, consisting of one word or several words, considered
 * as an abstract unit, and applied to a family of words related by form or meaning.
 */
public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
