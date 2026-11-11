package parser;

import lexer.Token;
import lexer.TokenType;
import codegen.SymbolTable;
import codegen.VMWriter;

public class GrammarDeclarations {

    private final Parser parser;
    private final GrammarStatements statements;
    private final SymbolTable symbolTable;
    private final VMWriter vmWriter;
    private String currentClassName;

    public GrammarDeclarations(Parser parser, SymbolTable symbolTable, VMWriter vmWriter) {
        this.parser = parser;
        this.symbolTable = symbolTable;
        this.vmWriter = vmWriter;
        
        // Repassa SymbolTable e VMWriter para os statements
        this.statements = new GrammarStatements(parser, symbolTable, vmWriter);
    }

    public void compileClass() {
        consumeKeyword("class");
        Token classToken = consume(TokenType.IDENTIFIER);
        this.currentClassName = classToken.getValue();
        
        statements.setCurrentClassName(currentClassName);

        consumeSymbol("{");

        while (checkKeyword("static") || checkKeyword("field")) {
            compileClassVarDec();
        }

        while (checkKeyword("constructor") || checkKeyword("function") || checkKeyword("method")) {
            compileSubroutine();
        }

        consumeSymbol("}");
        vmWriter.close();
    }

    public void compileClassVarDec() {
        Token kindToken = advance(); // "static" ou "field"
        SymbolTable.Kind kind = kindToken.getValue().equals("static") 
                ? SymbolTable.Kind.STATIC 
                : SymbolTable.Kind.FIELD;

        Token typeToken = consumeType();
        Token nameToken = consume(TokenType.IDENTIFIER);

        // Define a primeira variável da linha
        symbolTable.define(nameToken.getValue(), typeToken.getValue(), kind);

        // Suporte a declarações múltiplas na mesma linha (ex: field int x, y, z;)
        while (checkSymbol(",")) {
            advance();
            Token nextName = consume(TokenType.IDENTIFIER);
            symbolTable.define(nextName.getValue(), typeToken.getValue(), kind);
        }

        consumeSymbol(";");
    }

    public void compileSubroutine() {
        Token subroutineKind = advance(); // "constructor", "function" ou "method"
        
        // Limpa o escopo local na SymbolTable
        symbolTable.startSubroutine();

        // Se for um método, o primeiro argumento (ARG 0) é a referência 'this'
        if (subroutineKind.getValue().equals("method")) {
            symbolTable.define("this", currentClassName, SymbolTable.Kind.ARG);
        }

        // Consome tipo de retorno (void ou tipo)
        if (checkKeyword("void")) {
            advance();
        } else {
            consumeType();
        }

        Token nameToken = consume(TokenType.IDENTIFIER);
        String subroutineName = currentClassName + "." + nameToken.getValue();

        consumeSymbol("(");
        compileParameterList(); // Popula os argumentos na SymbolTable (ARG)
        consumeSymbol(")");

        // Processa o corpo da subrotina (declarações var locais e instruções)
        compileSubroutineBody(subroutineName, subroutineKind.getValue());
    }

    public void compileParameterList() {
        if (!checkSymbol(")")) {
            Token type = consumeType();
            Token name = consume(TokenType.IDENTIFIER);
            symbolTable.define(name.getValue(), type.getValue(), SymbolTable.Kind.ARG);

            while (checkSymbol(",")) {
                advance();
                Token nextType = consumeType();
                Token nextName = consume(TokenType.IDENTIFIER);
                symbolTable.define(nextName.getValue(), nextType.getValue(), SymbolTable.Kind.ARG);
            }
        }
    }

    public void compileSubroutineBody(String subroutineName, String subroutineKind) {
        consumeSymbol("{");

        // Processa variáveis locais (VAR)
        while (checkKeyword("var")) {
            compileVarDec();
        }

        // Emite o cabeçalho da função com o número exato de variáveis locais salvas
        int nVars = symbolTable.varCount(SymbolTable.Kind.VAR);
        vmWriter.writeFunction(subroutineName, nVars);

        // Lógica de alocação de memória / alinhamento do ponteiro 'this'
        if (subroutineKind.equals("constructor")) {
            // Aloca espaço para os atributos da classe (FIELD)
            int nFields = symbolTable.varCount(SymbolTable.Kind.FIELD);
            vmWriter.writePush(VMWriter.Segment.CONST, nFields);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0); // Aponta THIS para o objeto criado
        } else if (subroutineKind.equals("method")) {
            // Mapeia o argumento 0 no ponteiro THIS
            vmWriter.writePush(VMWriter.Segment.ARG, 0);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);
        }

        statements.compileStatements();
        consumeSymbol("}");
    }

    public void compileVarDec() {
        consumeKeyword("var");
        Token type = consumeType();
        Token name = consume(TokenType.IDENTIFIER);
        symbolTable.define(name.getValue(), type.getValue(), SymbolTable.Kind.VAR);

        while (checkSymbol(",")) {
            advance();
            Token nextName = consume(TokenType.IDENTIFIER);
            symbolTable.define(nextName.getValue(), type.getValue(), SymbolTable.Kind.VAR);
        }

        consumeSymbol(";");
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

    private Token consumeType() {
        Token token = peek();

        if (token == null) {
            throw syntaxError("Esperado um tipo de variável.");
        }

        // 1. Verifica se é um tipo primitivo (int, char, boolean)
        if (checkKeyword("int") || checkKeyword("char") || checkKeyword("boolean")) {
            return advance();
        }

        // 2. Verifica se é o nome de uma classe (Identificador)
        if (token.getType() == TokenType.IDENTIFIER) {
            return advance();
        }

        throw syntaxError(
            "Esperado 'int', 'char', 'boolean' ou um nome de classe, mas foi encontrado: '" 
            + token.getValue() + "'"
        );
    }
}