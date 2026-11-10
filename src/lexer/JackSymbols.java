package lexer;

import java.util.Set;

public class JackSymbols {

    private static final Set<Character> SYMBOLS = Set.of(
        '{', '}', '(', ')',
        '[', ']',
        '.', ',', ';',
        '+', '-', '*', '/',
        '&', '|', '<', '>',
        '=', '~'
    );

    public static boolean isSymbol(char c) {
        return SYMBOLS.contains(c);
    }
}