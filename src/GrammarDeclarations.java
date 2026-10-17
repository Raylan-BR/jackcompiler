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
            compileClassVarDec();
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
    
    public void compileClassVarDec() {
        // Reconhece a palavra-chave 'static' ou 'field'.
        if (!checkKeyword("static") && !checkKeyword("field")) {
            throw syntaxError("Expected 'static' or 'field'.");
        }

        advance();

        // Reconhece o tipo da variável.
        consumeType();

        // Lê o nome da primeira variável.
        consume(TokenType.IDENTIFIER);

        // Lê os nomes das variáveis adicionais, se houver.
        while (checkSymbol(",")) {
            advance();
            consume(TokenType.IDENTIFIER);
        }

        // Reconhece o ponto e vírgula no final da declaração.
        consumeSymbol(";");
    }

    private Token consumeType() {
        Token token = peek();

        if (token == null) {
            throw syntaxError("Expected a type.");
        }

        if (checkKeyword("int")
                || checkKeyword("char")
                || checkKeyword("boolean")) {
            return advance();
        }

        if (token.getType() == TokenType.IDENTIFIER) {
            return advance();
        }

        throw syntaxError("Expected int, char, boolean, or a type identifier.");
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