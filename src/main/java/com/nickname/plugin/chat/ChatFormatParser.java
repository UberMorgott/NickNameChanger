package com.nickname.plugin.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFormatParser {

    public enum TokenType { LITERAL, PLACEHOLDER }

    public static class Token {
        public final TokenType type;
        public final String value;

        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(prefix|suffix|username|message)\\}");

    private final List<Token> tokens;

    public ChatFormatParser(String format) {
        this.tokens = Collections.unmodifiableList(tokenize(format));
    }

    public List<Token> getTokens() {
        return tokens;
    }

    private static List<Token> tokenize(String format) {
        List<Token> result = new ArrayList<>();
        Matcher m = PLACEHOLDER.matcher(format);
        int last = 0;

        while (m.find()) {
            if (m.start() > last) {
                result.add(new Token(TokenType.LITERAL, format.substring(last, m.start())));
            }
            result.add(new Token(TokenType.PLACEHOLDER, m.group(1)));
            last = m.end();
        }

        if (last < format.length()) {
            result.add(new Token(TokenType.LITERAL, format.substring(last)));
        }

        return result;
    }
}
