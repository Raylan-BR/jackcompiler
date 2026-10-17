public class GrammarDeclarations {

    private final Parser parser;

    // Recebe o parser responsável pelos tokens.
    public GrammarDeclarations(Parser parser) {
        if (parser == null) {
            throw new IllegalArgumentException(
                    "O parser não pode ser nulo.");
        }

        this.parser = parser;
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

    // Verifica se o token atual é uma palavra-chave.
    private boolean checkKeyword(String value) {
        Token token = peek();

        return token != null
                && token.getType() == TokenType.KEYWORD
                && token.getValue().equals(value);
    }

    private void consumeKeyword(String value) {
        Token token = peek();

        if (token == null
                || token.getType() != TokenType.KEYWORD
                || !token.getValue().equals(value)) {
            throw syntaxError("esperado a palavra-chave '" + value + "'");
        }

        advance();
    }

    // Verifica se o token atual é um símbolo.
    private void consumeSymbol(String value) {
        Token token = peek();

        if (token == null
                || token.getType() != TokenType.SYMBOL
                || !token.getValue().equals(value)) {
            throw syntaxError("esperado o símbolo '" + value + "'");
        }

        advance();
    }

    private RuntimeException syntaxError(String message) {
        Token token = peek();

        if (token == null) {
            return new RuntimeException(
                    "Erro de sintaxe no fim do arquivo: " + message + ".");
        }

        return new RuntimeException(
                "Erro de sintaxe na linha " + token.getLine()
                        + ", coluna " + token.getColumn()
                        + ": " + message + ". Encontrado '"
                        + token.getValue() + "'.");
    }

    public void compile_class_var_dec() {
        // A declaração deve começar com static ou field.
        if (!checkKeyword("static") && !checkKeyword("field")) {
            throw syntaxError("esperado 'static' ou 'field'");
        }

        advance();

        // Tipo da variável: int, char, boolean ou nome de classe.
        consumeType();

        // Nome da primeira variável.
        consume(TokenType.IDENTIFIER);

        // Nomes adicionais separados por vírgula.
        while (checkSymbol(",")) {
            advance();
            consume(TokenType.IDENTIFIER);
        }

        // Toda declaração deve terminar com ponto e vírgula.
        consumeSymbol(";");
    }

    public void compile_subroutine() {
        // Tipo da sub-rotina.
        if (!checkKeyword("constructor")
                && !checkKeyword("function")
                && !checkKeyword("method")) {
            throw syntaxError(
                    "esperado 'constructor', 'function' ou 'method'");
        }

        advance();

        // Tipo de retorno: void ou um tipo válido.
        if (checkKeyword("void")) {
            advance();
        } else {
            consumeType();
        }

        // Nome da sub-rotina.
        consume(TokenType.IDENTIFIER);

        // Lista de parâmetros entre parênteses.
        consumeSymbol("(");
        compile_parameter_list();
        consumeSymbol(")");

        // Corpo delimitado por chaves.
        compile_subroutine_body();
    }

    public void compile_parameter_list() {
        // Lista vazia é válida: method void reset() { ... }
        if (checkSymbol(")")) {
            return;
        }

        // Primeiro parâmetro.
        consumeType();
        consume(TokenType.IDENTIFIER);

        // Parâmetros adicionais.
        while (checkSymbol(",")) {
            advance();
            consumeType();
            consume(TokenType.IDENTIFIER);
        }
    }

    public void compile_subroutine_body() {
        consumeSymbol("{");

        // Declarações locais: var int x;
        while (checkKeyword("var")) {
            compile_var_dec();
        }

        // O processamento dos comandos será implementado
        // posteriormente pelo módulo responsável pelos statements.
        while (!checkSymbol("}") && !isAtEnd()) {
            throw syntaxError(
                    "comandos do corpo ainda não são reconhecidos");
        }

        consumeSymbol("}");
    }

    public void compile_var_dec() {
        consumeKeyword("var");
        consumeType();
        consume(TokenType.IDENTIFIER);

        while (checkSymbol(",")) {
            advance();
            consume(TokenType.IDENTIFIER);
        }

        consumeSymbol(";");
    }

    private void consumeType() {
        Token token = peek();

        if (token == null) {
            throw syntaxError("esperado um tipo");
        }

        // Tipos primitivos permitidos pela gramática Jack.
        if (checkKeyword("int")
                || checkKeyword("char")
                || checkKeyword("boolean")) {
            advance();
            return;
        }

        // Um identificador também pode representar um tipo de classe.
        if (token.getType() == TokenType.IDENTIFIER) {
            advance();
            return;
        }

        throw syntaxError(
                "esperado um tipo (int, char, boolean ou nome de classe)");
    }

    private boolean checkSymbol(String value) {
        Token token = peek();

        return token != null
                && token.getType() == TokenType.SYMBOL
                && token.getValue().equals(value);
    }

    // Compila a regra da classe.
    public void compile_class() {
        // class
        consumeKeyword("class");

        // Nome da classe
        consume(TokenType.IDENTIFIER);

        // {
        consumeSymbol("{");

        // Declarações de variáveis de classe
        while (checkKeyword("static") || checkKeyword("field")) {
            compile_class_var_dec();
        }

        // Declarações de sub-rotinas
        while (checkKeyword("constructor")
                || checkKeyword("function")
                || checkKeyword("method")) {
            compile_subroutine();
        }

        // }
        consumeSymbol("}");

        // Não deve haver tokens extras após a classe
        if (!isAtEnd()) {
            throw syntaxError("tokens inesperados após o fechamento da classe");
        }
    }
}