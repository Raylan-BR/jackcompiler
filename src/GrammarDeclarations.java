public class GrammarDeclarations {

    private final Parser parser;
    private final GrammarStatements statements;
    private final SyntaxXmlWriter xmlWriter;

    public GrammarDeclarations(Parser parser, SyntaxXmlWriter xmlWriter) {
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
        this.statements = new GrammarStatements(parser, xmlWriter);
    }

    public void compileClass() {
        xmlWriter.openTag("class");

        consumeKeyword("class");
        consume(TokenType.IDENTIFIER);
        consumeSymbol("{");

        while (checkKeyword("static") || checkKeyword("field")) {
            compileClassVarDec();
        }

        while (checkKeyword("constructor")
                || checkKeyword("function")
                || checkKeyword("method")) {
            compileSubroutine();
        }

        consumeSymbol("}");

        if (!isAtEnd()) {
            throw syntaxError(
                "Tokens restantes após o fechamento da classe."
            );
        }

        xmlWriter.closeTag("class");
    }

    public void compileClassVarDec() {
        if (!checkKeyword("static") && !checkKeyword("field")) {
            throw syntaxError("Expected 'static' or 'field'.");
        }

        xmlWriter.openTag("classVarDec");

        advance();
        consumeType();
        consume(TokenType.IDENTIFIER);

        while (checkSymbol(",")) {
            advance();
            consume(TokenType.IDENTIFIER);
        }

        consumeSymbol(";");

        xmlWriter.closeTag("classVarDec");
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

        throw syntaxError(
            "Expected int, char, boolean, or a type identifier."
        );
    }

    public void compileSubroutine() {
        if (!checkKeyword("constructor")
                && !checkKeyword("function")
                && !checkKeyword("method")) {
            throw syntaxError(
                "Expected 'constructor', 'function', or 'method'."
            );
        }

        xmlWriter.openTag("subroutineDec");

        advance();

        if (checkKeyword("void")) {
            advance();
        } else {
            consumeType();
        }

        consume(TokenType.IDENTIFIER);
        consumeSymbol("(");
        compileParameterList();
        consumeSymbol(")");
        compileSubroutineBody();

        xmlWriter.closeTag("subroutineDec");
    }

    public void compileParameterList() {
        xmlWriter.openTag("parameterList");

        if (!checkSymbol(")")) {
            consumeType();
            consume(TokenType.IDENTIFIER);

            while (checkSymbol(",")) {
                advance();
                consumeType();
                consume(TokenType.IDENTIFIER);
            }
        }

        xmlWriter.closeTag("parameterList");
    }

    public void compileSubroutineBody() {
        xmlWriter.openTag("subroutineBody");

        consumeSymbol("{");

        while (checkKeyword("var")) {
            compileVarDec();
        }

        statements.compileStatements();
        consumeSymbol("}");

        xmlWriter.closeTag("subroutineBody");
    }

    public void compileVarDec() {
        xmlWriter.openTag("varDec");

        consumeKeyword("var");
        consumeType();
        consume(TokenType.IDENTIFIER);

        while (checkSymbol(",")) {
            advance();
            consume(TokenType.IDENTIFIER);
        }

        consumeSymbol(";");

        xmlWriter.closeTag("varDec");
    }

    public void consumeKeyword(String expected) {
        Token token = peek();

        if (token == null
                || token.getType() != TokenType.KEYWORD
                || !token.getValue().equals(expected)) {
            throw syntaxError("Expected keyword: " + expected);
        }

        advance();
    }

    public void consumeSymbol(String expected) {
        Token token = peek();

        if (token == null
                || token.getType() != TokenType.SYMBOL
                || !token.getValue().equals(expected)) {
            throw syntaxError("Expected symbol: " + expected);
        }

        advance();
    }

    public boolean checkKeyword(String expected) {
        Token token = peek();

        return token != null
            && token.getType() == TokenType.KEYWORD
            && token.getValue().equals(expected);
    }

    public boolean checkSymbol(String expected) {
        Token token = peek();

        return token != null
            && token.getType() == TokenType.SYMBOL
            && token.getValue().equals(expected);
    }

    private RuntimeException syntaxError(String message) {
        Token token = peek();

        String tokenInfo = token != null
            ? " at token '" + token.getValue() + "'"
            : " at end of input";

        return new RuntimeException(
            "Syntax error" + tokenInfo + ": " + message
        );
    }

    public boolean isAtEnd() {
        return parser.isAtEnd();
    }

    public Token peek() {
        return parser.peek();
    }

    public Token advance() {
        return parser.advance();
    }

    public boolean match(TokenType type) {
        return parser.match(type);
    }

    public Token consume(TokenType type) {
        return parser.consume(type);
    }
}