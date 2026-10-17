import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Verifica se todos os tokens foram consumidos.
    boolean isAtEnd() {
        return current >= tokens.size();
    }

    // Retorna o token atual sem avançar.
    Token peek() {
        if (isAtEnd()) {
            return null;
        }

        return tokens.get(current);
    }

    // Consome o token atual e avança para o próximo.
    Token advance() {
        if (isAtEnd()) {
            return null;
        }

        return tokens.get(current++);
    }

    // Verifica se o token atual é de determinado tipo.
    boolean match(TokenType type) {
        if (isAtEnd() || peek().getType() != type) {
            return false;
        }

        if (peek().getType() != type) {
            return false;
        }

        advance();
        return true;
    }

    // Consome o token atual e avança para o próximo.
    Token consume(TokenType type) {
        if (isAtEnd()) {
            throw new RuntimeException(
                    "Erro de sintaxe: esperado " + type
                            + ", mas o arquivo terminou.");
        }

        if (peek().getType() == type) {
            return advance();
        }

        Token token = peek();

        throw new RuntimeException(
            "Erro de sintaxe na linha " + token.getLine()
                    + ", coluna " + token.getColumn()
                    + ": esperado " + type
                    + ", mas encontrado '" + token.getValue() + "'.");
    }
}
