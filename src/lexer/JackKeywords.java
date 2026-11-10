package lexer;
import java.util.Set;

public final class JackKeywords {

    private JackKeywords() {
    }

    public static final Set<String> KEYWORDS = Set.of(
            "class",
            "constructor",
            "function",
            "method",
            "field",
            "static",
            "var",
            "int",
            "char",
            "boolean",
            "void",
            "true",
            "false",
            "null",
            "this",
            "let",
            "do",
            "if",
            "else",
            "while",
            "return"
    );

    public static boolean isKeyword(String value) {
        return KEYWORDS.contains(value);
    }
}