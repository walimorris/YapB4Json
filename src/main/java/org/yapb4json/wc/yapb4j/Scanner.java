package org.yapb4json.wc.yapb4j;

import java.util.ArrayList;
import java.util.List;

import static org.yapb4json.wc.yapb4j.TokenType.*;

/**
 * In the Scanner, the raw json code is stored as a simple string, and a {@link List}
 * is provided to fill up with tokens that'll be generated. What is <b>Lexical grammar</b>?
 * Lexical grammar, in computer science, is a formal grammar defining the syntax of token.
 * The program is written using characters that are defined by the lexical structure of the
 * language used. The lexical grammar lays down the rules governing how a character
 * sequence is divided up into subsequences of characters, each part representing an
 * individual token. Are you thinking about regular expressions?
 * <p>
 * The Scanner class helps lay the rules for Yapb4j and the methods to consume tokens within
 * the framework of its lexical grammar for our definition of JavaScript Object Notation (JSON).
 *
 * @see Yapb4j
 * @see #scanToken()
 * @see #scanTokens()
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

    /**
     * Tracks the colon between a key/value pair. The colon can be reset to false
     * once a comma is parsed. A parsed comma tells the scanner to prepare for
     * more key/values.
     */
    private static boolean colonSet = false;

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
        if (start == 0) {
            checkStartToken();
        }
        char c = nextToken();
        switch (c) {
            case '{':
                addToken(RIGHT_BRACKET);
                break;
            case '}':
                addToken(LEFT_BRACKET);
                break;
            case ':':
                addToken(COLON);
                break;
            case ',':
                addToken(COMMA);
                break;

            // ignore whitespace
            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                break;

            // dealing with string literals
            case '"':
                stringLiteral();
                if (!colonSet) {
                    peekForPreColon();
                } else {
                    peekForPostColon();
                }
                break;

            // let's report errors outside our parser's
            default:
                if (isDigit(c)) {
                    numberLiteral();
                } else {
                    Yapb4j.error(line, String.format("Unexpected character '%c'", c));
                    System.exit(1);
                }
                break;
        }
    }

    /**
     * Validate starting character in json source. Prints error to STDOUT
     * and exits if the incorrect starting structure is parsed.
     */
    private void checkStartToken() {
        char c = source.charAt(0);
        if (!(c == '{')) {
            Yapb4j.error(line, String.format("Expected character '{', but got '%c'.", c));
            System.exit(1);
        }
    }

    private void isEndOrContinue() {

    }

    /**
     * Consumes as many digits as found for the integer part of the literal. A
     * fractional part is then scanned for (the decimal), followed by atleast
     * one digit. We need to look ahead and past the decimal point to ensure
     * we have a decimal after the "."
     */
    private void numberLiteral() {
        while (isDigit(peek())) {
            nextToken();

            // look for fractional part.
            if (peek() == '.' && isDigit(peekNext())) {
                // consume the "."
                nextToken();
                while (isDigit(peek())) {
                    nextToken();
                }
            }
        }
        addToken(NUMERIC, Double.parseDouble(source.substring(start, current)));

    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Consumes tokens until the closing quotations is consumed. This ends
     * the string literal. An error is reported if the source is ended
     * before the closing quotation is consumed.
     */
    private void stringLiteral() {
        while (peek() != '"' && !scanIsAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            nextToken();
        }

        if (scanIsAtEnd()) {
            Yapb4j.error(line, "Invalid json string literal.");
            return;
        }
        nextToken();

        // trim surround quotations and produce the string literal value
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * Similar to {@link Scanner#nextToken()} but doesn't consume the character.
     * This is considered a look-a-head.
     *
     * @return boolean
     */
    private char peek() {
        if (scanIsAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private void peekForColon(char c) {
        while (peek() != c && !scanIsAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            nextToken();
        }
    }

    private void peekForPreColon() {
        peekForColon(':');
        if (scanIsAtEnd()) {
            Yapb4j.error(line, "Invalid json. Expected ':'.");
            System.exit(1);
        }

        // set colon for colon lookout - can reset when comma is parsed
        // and there are more values
        colonSet = true;
    }

    //TODO: peek for post colon character before parsing remaining values
    // or end EOF character
    private  void peekForPostColon() {
        peekForColon('"');
        if (scanIsAtEnd()) {
            Yapb4j.error(line, "Invalid json. Expected '\"'. ");
            System.exit(1);
        }
    }

    /**
     * This is a look-a-head, and more so a double look-a-head.
     *
     * @see #peek()
     *
     * @return char
     */
    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
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
     * Reports the current token.
     *
     * @return char
     */
    private char currentToken() {
        return source.charAt(current);
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
