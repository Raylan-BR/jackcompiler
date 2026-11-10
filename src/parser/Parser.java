package parser;

import java.util.List;

import lexer.Token;
import lexer.TokenType;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        if (tokens == null) {
            throw new IllegalArgumentException(
                "A lista de tokens não pode ser nula."
            );
        }

        this.tokens = tokens;
    }

    public boolean isAtEnd() {
        return current >= tokens.size();
    }

    public Token peek() {
        if (isAtEnd()) {
            return null;
        }

        return tokens.get(current);
    }

    public Token advance() {
        if (isAtEnd()) {
            return null;
        }

        return tokens.get(current++);
    }

    public boolean match(TokenType type) {
        if (isAtEnd() || peek().getType() != type) {
            return false;
        }

        advance();
        return true;
    }

    public Token consume(TokenType type) {
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
}