package org.yapb4json.wc.yapb4j;

public enum TokenType {
    // Single-character tokens.
    LEFT_BRACKET, RIGHT_BRACKET, COLON, COMMA, PARENTHESIS,

    // Literal Value tokens.
    STRING, NUMERIC, BOOL, NULL,

    EOF
}
