import java.util.List;

public class GrammarStatements {

    private final Parser parser;
    private final GrammarExpressionsTerms expressions;
    private final SyntaxXmlWriter xmlWriter;

    public GrammarStatements(
            Parser parser,
            SyntaxXmlWriter xmlWriter) {

        if (parser == null) {
            throw new IllegalArgumentException(
                "Parser não pode ser nulo."
            );
        }

        if (xmlWriter == null) {
            throw new IllegalArgumentException(
                "Writer XML não pode ser nulo."
            );
        }

        this.parser = parser;
        this.xmlWriter = xmlWriter;
        this.expressions =
            new GrammarExpressionsTerms(parser, xmlWriter);
    }

    public GrammarStatements(List<Token> tokens) {
        this(new Parser(tokens), null);
    }

    public Parser getParser() {
        return parser;
    }

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

    public void compileStatements() {
        xmlWriter.openTag("statements");

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

                default:
                    throw new IllegalStateException(
                        "Comando não reconhecido: " + command
                    );
            }
        }

        xmlWriter.closeTag("statements");
    }

    public void compileLet() {
        xmlWriter.openTag("letStatement");

        consumeKeyword("let");
        parser.consume(TokenType.IDENTIFIER);

        if (checkSymbol("[")) {
            consumeSymbol("[");
            expressions.compileExpression();
            consumeSymbol("]");
        }

        consumeSymbol("=");
        expressions.compileExpression();
        consumeSymbol(";");

        xmlWriter.closeTag("letStatement");
    }

    public void compileIf() {
        xmlWriter.openTag("ifStatement");

        consumeKeyword("if");
        compileConditionAndBlock();

        if (checkKeyword("else")) {
            consumeKeyword("else");
            consumeSymbol("{");
            compileStatements();
            consumeSymbol("}");
        }

        xmlWriter.closeTag("ifStatement");
    }

    public void compileWhile() {
        xmlWriter.openTag("whileStatement");

        consumeKeyword("while");
        compileConditionAndBlock();

        xmlWriter.closeTag("whileStatement");
    }

    public void compileDo() {
        xmlWriter.openTag("doStatement");

        consumeKeyword("do");
        compileSubroutineCall();
        consumeSymbol(";");

        xmlWriter.closeTag("doStatement");
    }

    public void compileReturn() {
        xmlWriter.openTag("returnStatement");

        consumeKeyword("return");

        if (!checkSymbol(";")) {
            expressions.compileExpression();
        }

        consumeSymbol(";");

        xmlWriter.closeTag("returnStatement");
    }

    private void compileConditionAndBlock() {
        consumeSymbol("(");
        expressions.compileExpression();
        consumeSymbol(")");
        consumeSymbol("{");
        compileStatements();
        consumeSymbol("}");
    }

    private void compileSubroutineCall() {
        parser.consume(TokenType.IDENTIFIER);

        if (checkSymbol(".")) {
            consumeSymbol(".");
            parser.consume(TokenType.IDENTIFIER);
        }

        consumeSymbol("(");
        expressions.compileExpressionList();
        consumeSymbol(")");
    }

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