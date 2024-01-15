package org.yapb4json.wc.yapb4j;

import java.util.ArrayList;
import java.util.List;

import static org.yapb4json.wc.yapb4j.TokenType.EOF;

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
            scanTokens();
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
}
