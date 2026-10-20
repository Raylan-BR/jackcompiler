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
        consumeKeyword("let");
        parser.consume(TokenType.IDENTIFIER);

        // Atribuição a um elemento de array.
        if (checkSymbol("[")) {
            consumeSymbol("[");
            expressions.compileExpression();
            consumeSymbol("]");
        }

        consumeSymbol("=");
        expressions.compileExpression();
        consumeSymbol(";");
    }

    public void compileIf() {
    }

    public void compileWhile() {
    }

    public void compileDo() {
    }

    public void compileReturn() {
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

    // Monta o erro de sintaxe no mesmo formato usado pelo Parser.
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