public class GrammarDeclarations {

    private final Parser parser;

    // Recebe o parser responsável pelos tokens.
    public GrammarDeclarations(Parser parser) {
        if (parser == null) {
            throw new IllegalArgumentException(
                "O parser não pode ser nulo."
            );
        }

        this.parser = parser;
    }

    public void compileClass() {
        // Reconhece a palavra-chave 'class'
        consumeKeyword("class");

        // Ler o identificador do nome da classe
        consume(TokenType.IDENTIFIER);

        // Reconhece o símbolo de abertura da classe
        consumeSymbol("{");

        // Precessa as declarações de variáveis da classe
        while (checkKeyword("static") || checkKeyword("field")) {
            compile_class_var_dec();
        }

        // Processa as declarações de sub-rotinas da classe
        while (checkKeyword("constructor")
                || checkKeyword("function")
                || checkKeyword("method")) {
            compileSubroutine();
        }

        // Reconhece o símbolo de fechamento da classe
        consumeSymbol("}");

        // Verifica se todos os tokens foram consumidos.
        if (!isAtEnd()) {
            throw syntaxError("Tokens restantes após o fechamento da classe.");
        }
    }

    // Verifica se todos os tokens foram consumidos.
    public boolean isAtEnd() {
        return parser.isAtEnd();
    }

    // Consulta o token atual sem consumi-lo.
    public Token peek() {
        return parser.peek();
    }

    // Consome o token atual e avança para o próximo.
    public Token advance() {
        return parser.advance();
    }

    // Verifica o tipo do token atual e o consome se corresponder.
    public boolean match(TokenType type) {
        return parser.match(type);
    }

    // Consome o token se ele for do tipo esperado.
    // Caso contrário, propaga o erro de sintaxe.
    public Token consume(TokenType type) {
        return parser.consume(type);
    }
}