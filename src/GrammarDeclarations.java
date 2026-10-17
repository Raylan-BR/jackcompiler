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

    // Compila uma declaração de classe, incluindo variáveis e sub-rotinas.
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
    
    // Consome a palavra -chave esperada, caso contrário, lança umerro de sintaxe.
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

    // Verifica se o token atual é do tipo esperado e o consome.
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
    
    // Verifica se o token atual é uma palavra-chave específica.
    public void compileSubroutine() {
        // Verifica se o token atual é uma palavra-chave de sub-rotina válida.
        if (!checkKeyword("constructor")
                && !checkKeyword("function")
                && !checkKeyword("method")) {
            throw syntaxError(
                "Expected 'constructor', 'function', or 'method'."
            );
        }

        advance();

        // Verifica se o tipo de retorno é 'void' ou um tipo válido.
        if (checkKeyword("void")) {
            advance();
        } else {
            consumeType();
        }

        // Lê o nome da sub-rotina.
        consume(TokenType.IDENTIFIER);

        // Reconhece o símbolo de abertura do parâmetro da sub-rotina.
        consumeSymbol("(");

        // Processa a lista de parâmetros da sub-rotina.
        compileParameterList();

        // Reconhece o parêntese de fechamento.
        consumeSymbol(")");

        // Processa o corpo da sub-rotina.
        compileSubroutineBody();
    }
    
    // Compila a lista de parâmetros de uma sub-rotina.
    public void compileParameterList() {
        // Verifica se a lista de parâmetros está vazia.
        if (checkSymbol(")")) {
            return;
        }

        // Processa o primeiro parâmetro.
        consumeType();
        consume(TokenType.IDENTIFIER);

        // Processa os parâmetros adicionais, se houver.
        while (checkSymbol(",")) {
            advance();

            consumeType();
            consume(TokenType.IDENTIFIER);
        }
    }

    // Compila o corpo de uma sub-rotina, incluindo declarações de variáveis locais e instruções.
    public void compileSubroutineBody() {
        // Reconhece o símbolo de abertura do corpo da sub-rotina.
        consumeSymbol("{");

        // Processa as declarações de variáveis locais da sub-rotina.
        while (checkKeyword("var")) {
            compileVarDec();
        }

        // Até que a análise de instruções seja implementada, rejeita tokens inesperados.
        if (!checkSymbol("}")) {
            throw syntaxError(
                "Unexpected token in subroutine body. "
                + "Statement parsing may not be implemented yet."
            );
        }

        // Reconhece o símbolo de fechamento do corpo da sub-rotina.
        consumeSymbol("}");
    }

    // Compila uma declaração de variável local, incluindo o tipo e os nomes das variáveis.
    public void compileVarDec() {
        // Reconhece a palavra-chave 'var'.
        consumeKeyword("var");

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

    // Verifica se o token atual é uma palavra-chave específica e a consome.
    public void consumeKeyword(String expected) {
        Token token = peek();

        if (token == null || token.getType() != TokenType.KEYWORD || !token.getValue().equals(expected)) {
            throw syntaxError("Expected keyword: " + expected);
        }

        advance();
    }

    // Verifica se o token atual é um símbolo específico e o consome.
    public void consumeSymbol(String expected) {
        Token token = peek();

        if (token == null || token.getType() != TokenType.SYMBOL || !token.getValue().equals(expected)) {
            throw syntaxError("Expected symbol: " + expected);
        }

        advance();
    }

    // Verifica a palavra-chave atual sem consumi-la.
    public boolean checkKeyword(String expected) {
        Token token = peek();
        return token != null && token.getType() == TokenType.KEYWORD && token.getValue().equals(expected);
    }

    // Verifica o símbolo atual sem consumi-lo.
    public boolean checkSymbol(String expected) {
        Token token = peek();
        return token != null && token.getType() == TokenType.SYMBOL && token.getValue().equals(expected);
    }

    // Gera uma exceção de erro de sintaxe com uma mensagem detalhada.
    private RuntimeException syntaxError(String message) {
        Token token = peek();
        String tokenInfo = (token != null) ? " at token '" + token.getValue() + "'" : " at end of input";
        return new RuntimeException("Syntax error" + tokenInfo + ": " + message);
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