
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private final SyntaxXmlWriter xmlWriter;
    private int current = 0;

    // Mantém compatibilidade com o código existente.
    public Parser(List<Token> tokens) {
        this(tokens, null);
    }

    // Permite ativar a geração de XML.
    public Parser(List<Token> tokens, SyntaxXmlWriter xmlWriter) {
        if (tokens == null) {
            throw new IllegalArgumentException(
                "A lista de tokens não pode ser nula."
            );
        }

        this.tokens = tokens;
        this.xmlWriter = xmlWriter;
    }

    boolean isAtEnd() {
        return current >= tokens.size();
    }

    Token peek() {
        if (isAtEnd()) {
            return null;
        }

        return tokens.get(current);
    }

    Token advance() {
        if (isAtEnd()) {
            return null;
        }

        Token token = tokens.get(current++);

        if (xmlWriter != null) {
            xmlWriter.writeToken(
                getXmlTokenType(token),
                token.getValue()
            );
        }

        return token;
    }

    boolean match(TokenType type) {
        if (isAtEnd() || peek().getType() != type) {
            return false;
        }

        advance();
        return true;
    }

    Token consume(TokenType type) {
        if (isAtEnd()) {
            throw new RuntimeException(
                "Erro de sintaxe: esperado " + type
                    + ", mas o arquivo terminou."
            );
        }

        if (peek().getType() == type) {
            return advance();
        }

        Token token = peek();

        throw new RuntimeException(
            "Erro de sintaxe na linha " + token.getLine()
                + ", coluna " + token.getColumn()
                + ": esperado " + type
                + ", mas encontrado '" + token.getValue() + "'."
        );
    }

    private String getXmlTokenType(Token token) {
        switch (token.getType()) {
            case KEYWORD:
                return "keyword";

            case SYMBOL:
                return "symbol";

            case IDENTIFIER:
                return "identifier";

            case INTEGER_CONSTANT:
                return "integerConstant";

            case STRING_CONSTANT:
                return "stringConstant";

            default:
                throw new IllegalArgumentException(
                    "Tipo de token desconhecido: "
                        + token.getType()
                );
        }
    }
}