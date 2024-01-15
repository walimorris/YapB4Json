package org.yapb4json.wc.yapb4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Fast scanner that can take any string of json source code and produce the
 * tokens that we’ll feed into the parser
 */
public class Yapb4j {
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: yapb4json [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Reads a file and feeds to {@link Yapb4j#run(String)} for token
     * processing.
     *
     * @param path {@link String} to file path
     * @throws IOException throws
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) {
            System.exit(65);
        }
    }

    /**
     * A prompt that reads input dynamically. When enter is processed the
     * reader will break and feed to {@link Yapb4j#run(String)} for token
     * processing.
     *
     * @throws IOException throws
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.println("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    /**
     * Creates a Scanner object and processes tokens from a source.
     *
     * @param source {@link String}
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // print the tokens
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    public static void error(int line, String message) {
        report (line, "", message);
    }

    /**
     * Error output that tells user some syntax error occurred on a given line.
     *
     * @param line error line
     * @param where
     * @param message
     */
    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
