import java.util.List;

// Responsável por reconhecer os comandos (statements) da linguagem Jack.
public class GrammarStatements {

    private final Parser parser;
    private final GrammarExpressionsTerms expressions;

    // Recebe o parser e compartilha o mesmo fluxo de tokens com as expressões.
    public GrammarStatements(Parser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("Parser não pode ser nulo");
        }
        this.parser = parser;
        this.expressions = new GrammarExpressionsTerms(parser);
    }

    // Construtor alternativo que recebe uma lista de tokens e cria um Parser.
    public GrammarStatements(List<Token> tokens) {
        this(new Parser(tokens));
    }

    public Parser getParser() {
        return parser;
    }

    /**
     * Identifica o comando atual sem consumir o token.
     *
     * Retorna let, if, while, do ou return.
     * Retorna null se o token não iniciar um comando.
     */
    public String identifyNextCommand() {
        Token token = parser.peek();

        if (token == null || token.getType() != TokenType.KEYWORD) {
            return null;
        }

        switch (token.getValue()) {
            case "let":
            case "if":
            case "while":
            case "do":
            case "return":
                return token.getValue();
            default:
                return null;
        }
    }

    /**
     * statements: statement*
     *
     * Para no primeiro token que não inicia um comando (por exemplo '}').
     * Quem chamou é responsável por exigir o token de fechamento.
     */
    public void compileStatements() {
        String command;

        while ((command = identifyNextCommand()) != null) {
            switch (command) {
                case "let":
                    compileLet();
                    break;
                case "if":
                    compileIf();
                    break;
                case "while":
                    compileWhile();
                    break;
                case "do":
                    compileDo();
                    break;
                case "return":
                    compileReturn();
                    break;
            }
        }
    }

    /**
     * let:
     * 'let' varName ('[' expression ']')? '=' expression ';'
     */
    public void compileLet() {
        // O comando let inicia com a palavra-chave 'let' seguida de um identificador
        consumeKeyword("let");
        parser.consume(TokenType.IDENTIFIER);

        // Atribuição a um elemento de array.
        if (checkSymbol("[")) {
            consumeSymbol("[");
            expressions.compileExpression();
            consumeSymbol("]");
        }

        // Atribuição a uma variável
        consumeSymbol("=");
        expressions.compileExpression();
        // O ponto e vírgula é obrigatório no final do comando let
        consumeSymbol(";");
    }

    /**
     * if:
     * 'if' '(' expression ')' '{' statements '}'
     * ('else' '{' statements '}')?
     */
    public void compileIf() {
        // Inicia com "if" seguido de uma expressão entre parênteses e um bloco de comandos entre chaves
        consumeKeyword("if");

        compileConditionAndBlock();

        // O bloco "else" é opcional, então veriofica se ele está presente antes de tentar consumi-lo
        if (checkKeyword("else")) {
            // Se o bloco "else" estiver presente, consume a palavra-chave "else"
            consumeKeyword("else");
            // Consome o bloco de comandos entre chaves
            consumeSymbol("{");
            // Compila os comandos dentro do bloco "else"
            compileStatements();
            // Consome a chave de fechamento do bloco "else"
            consumeSymbol("}");
        }
    }

    /**
     * while:
     * 'while' '(' expression ')' '{' statements '}'
     */
    public void compileWhile() {
        // O comando while inicia com 'while' 
        // seguida de uma expressão entre parênteses e um bloco de comandos entre chaves
        consumeKeyword("while");
        // Compila a expressão e o bloco de comandos do while
        compileConditionAndBlock();
    }

    /**
     * do:
     * 'do' subroutineCall ';'
     */
    public void compileDo() {
        // O comando do inicia com 'do'
        consumeKeyword("do");
        // Compila a chamada de sub-rotina (subroutine call) que é obrigatória no comando do
        compileSubroutineCall();
        // O ponto e vírgula é obrigatório no final do comando do
        consumeSymbol(";");
    }

    /**
     * return:
     * 'return' expression? ';'
     */
    public void compileReturn() {
        // O comando return inicia com 'return'
        consumeKeyword("return");

        // A expressão de retorno é opcional
        // verifica se há uma expressão antes de tentar compilá-la
        if (!checkSymbol(";")) {
            expressions.compileExpression();
        }

        // O ponto e vírgula é obrigatório no final do comando return
        consumeSymbol(";");
    }

    // Parte comum de if e while: '(' expression ')' '{' statements '}'
    private void compileConditionAndBlock() {
        consumeSymbol("(");
        expressions.compileExpression();
        consumeSymbol(")");
        consumeSymbol("{");
        compileStatements();
        consumeSymbol("}");
    }

    // subroutineName '(' expressionList ')'
    // | (className | varName) '.' subroutineName '(' expressionList ')'
    private void compileSubroutineCall() {
        parser.consume(TokenType.IDENTIFIER);

        if (checkSymbol(".")) {
            consumeSymbol(".");
            parser.consume(TokenType.IDENTIFIER);
        }

        consumeSymbol("(");
        compileExpressionList();
        consumeSymbol(")");
    }

    // (expression (',' expression)*)?
    private void compileExpressionList() {
        if (checkSymbol(")")) {
            return;
        }

        expressions.compileExpression();

        while (checkSymbol(",")) {
            consumeSymbol(",");
            expressions.compileExpression();
        }
    }

    // métodos auxiliares

    private boolean checkKeyword(String expected) {
        Token token = parser.peek();

        return token != null
            && token.getType() == TokenType.KEYWORD
            && token.getValue().equals(expected);
    }

    private boolean checkSymbol(String expected) {
        Token token = parser.peek();

        return token != null
            && token.getType() == TokenType.SYMBOL
            && token.getValue().equals(expected);
    }

    private void consumeKeyword(String expected) {
        if (!checkKeyword(expected)) {
            throw unexpected(expected);
        }
        parser.advance();
    }

    private void consumeSymbol(String expected) {
        if (!checkSymbol(expected)) {
            throw unexpected(expected);
        }
        parser.advance();
    }

    private RuntimeException unexpected(String expected) {
        Token token = parser.peek();

        if (token == null) {
            return new RuntimeException(
                "Erro de sintaxe: esperado '" + expected
                    + "', mas o arquivo terminou."
            );
        }

        return new RuntimeException(
            "Erro de sintaxe na linha " + token.getLine()
                + ", coluna " + token.getColumn()
                + ": esperado '" + expected
                + "', mas encontrado '" + token.getValue() + "'."
        );
    }
}