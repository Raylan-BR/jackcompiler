
public class GrammarExpressionsTerms {

    private final Parser parser;
    private final SyntaxXmlWriter xmlWriter;

    public GrammarExpressionsTerms(
            Parser parser,
            SyntaxXmlWriter xmlWriter) {

        if (parser == null) {
            throw new IllegalArgumentException(
                "O parser não pode ser nulo."
            );
        }

        if (xmlWriter == null) {
            throw new IllegalArgumentException(
                "O writer XML não pode ser nulo."
            );
        }

        this.parser = parser;
        this.xmlWriter = xmlWriter;
    }

    public void compileExpression() {
        xmlWriter.openTag("expression");

        compileTerm();

        while (compileOp()) {
            compileTerm();
        }

        xmlWriter.closeTag("expression");
    }

    public void compileTerm() {
        Token token = parser.peek();

        if (token == null) {
            throw new RuntimeException(
                "Erro de sintaxe: esperado um termo, mas o arquivo terminou."
            );
        }

        xmlWriter.openTag("term");

        switch (token.getType()) {
            case INTEGER_CONSTANT:
            case STRING_CONSTANT:
                parser.advance();
                break;

            case KEYWORD:
                if (!compileKeywordConstant()) {
                    throw new RuntimeException(
                        "Erro de sintaxe: constante de palavra-chave inválida: "
                            + token.getValue()
                    );
                }
                break;

            case IDENTIFIER:
                compileIdentifierTerm();
                break;

            case SYMBOL:
                if (compileUnaryOp()) {
                    compileTerm();
                } else if (isSymbol("(")) {
                    consumeSymbol("(");
                    compileExpression();
                    consumeSymbol(")");
                } else {
                    throw new RuntimeException(
                        "Erro de sintaxe na linha " + token.getLine()
                            + ", coluna " + token.getColumn()
                            + ": termo inválido: '" + token.getValue() + "'."
                    );
                }
                break;

            default:
                throw new RuntimeException(
                    "Erro de sintaxe: termo inválido: " + token.getValue()
                );
        }

        xmlWriter.closeTag("term");
    }

    public boolean compileOp() {
        Token token = parser.peek();

        if (token == null || token.getType() != TokenType.SYMBOL) {
            return false;
        }

        switch (token.getValue()) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "&":
            case "|":
            case "<":
            case ">":
            case "=":
                parser.advance();
                return true;

            default:
                return false;
        }
    }

    public boolean compileUnaryOp() {
        Token token = parser.peek();

        if (token == null || token.getType() != TokenType.SYMBOL) {
            return false;
        }

        String operator = token.getValue();

        if (operator.equals("-") || operator.equals("~")) {
            parser.advance();
            return true;
        }

        return false;
    }

    public boolean compileKeywordConstant() {
        Token token = parser.peek();

        if (token == null || token.getType() != TokenType.KEYWORD) {
            return false;
        }

        switch (token.getValue()) {
            case "true":
            case "false":
            case "null":
            case "this":
                parser.advance();
                return true;

            default:
                return false;
        }
    }

    private boolean isSymbol(String expected) {
        Token token = parser.peek();

        return token != null
            && token.getType() == TokenType.SYMBOL
            && token.getValue().equals(expected);
    }

    private void consumeSymbol(String expected) {
        if (!isSymbol(expected)) {
            Token token = parser.peek();

            if (token == null) {
                throw new RuntimeException(
                    "Erro de sintaxe: esperado '" + expected
                        + "', mas o arquivo terminou."
                );
            }

            throw new RuntimeException(
                "Erro de sintaxe na linha " + token.getLine()
                    + ", coluna " + token.getColumn()
                    + ": esperado '" + expected
                    + "', mas encontrado '" + token.getValue() + "'."
            );
        }

        parser.advance();
    }

    public void compileExpressionList() {
        xmlWriter.openTag("expressionList");

        if (!isSymbol(")")) {
            compileExpression();

            while (isSymbol(",")) {
                consumeSymbol(",");
                compileExpression();
            }
        }

        xmlWriter.closeTag("expressionList");
    }

    private void compileIdentifierTerm() {
        parser.consume(TokenType.IDENTIFIER);

        if (isSymbol("[")) {
            consumeSymbol("[");
            compileExpression();
            consumeSymbol("]");
        } else if (isSymbol("(")) {
            consumeSymbol("(");
            compileExpressionList();
            consumeSymbol(")");
        } else if (isSymbol(".")) {
            consumeSymbol(".");
            parser.consume(TokenType.IDENTIFIER);
            consumeSymbol("(");
            compileExpressionList();
            consumeSymbol(")");
        }
    }
}