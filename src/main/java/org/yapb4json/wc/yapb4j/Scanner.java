package org.yapb4json.wc.yapb4j;

import java.util.ArrayList;
import java.util.List;

import static org.yapb4json.wc.yapb4j.TokenType.*;

/**
 * In the Scanner, the raw json code is stored as a simple string, and a {@link List}
 * is provided to fill up with tokens that'll be generated.
 */
public class Scanner {
    private final String source;
    private static final List<Token> tokens = new ArrayList<>();

    /**
     * Points to first index in the string.
     */
    private static int start = 0;

    /**
     * Points to character currently being considered.
     */
    private static int current = 0;

    /**
     * Tracks what source line current is on to produce location aware tokens.
     */
    private static int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    /**
     * The scanner runs through the source, adding tokens until it runs out
     * of characters. Then it appends a final EOF token.
     *
     * @return {@link List<Token>}
     */
    public List<Token> scanTokens() {
        while (!scanIsAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Notifies scanner if all characters from source have been consumed.
     *
     * @return boolean
     */
    private boolean scanIsAtEnd() {
        return current >= source.length();
    }

    /**
     * Each turn of the loop, a single token is scanned. Yapb4j is super
     * lightweight. We don't have many complex token types.
     */
    private void scanToken() {
        char c = nextToken();
        switch (c) {
            case '{': addToken(RIGHT_BRACKET); break;
            case '}': addToken(LEFT_BRACKET); break;
            case ':': addToken(COLON); break;
            case ',': addToken(COMMA); break;
            case '"': addToken(QUOTATION); break;

            // let's report errors outside our parser's
            default:
                Yapb4j.error(line, "Unexpected character.");
                break;
        }
    }

    private boolean match(char expected) {
        if (scanIsAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    /**
     * Consumes the next character in the source and returns it.
     *
     * @return char
     */
    private char nextToken() {
        return source.charAt(current++);
    }

    /**
     * Takes character at current lexeme and creates a new token for it.
     *
     * @param type {@link TokenType}
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Used for token with literal values.
     *
     * @param type {@link TokenType}
     * @param literal value
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
