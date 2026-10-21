// O Token representa uma unidade léxica do código, contendo seu tipo e valor.

public class Token {

    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }


    // Retorna o tipo do token
    public TokenType getType() {
        return type;
    }

    // Retorna o valor do token
    public String getValue() {
        return value;
    }
}