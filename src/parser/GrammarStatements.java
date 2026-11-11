package parser;

import lexer.Token;
import lexer.TokenType;
import codegen.SymbolTable;
import codegen.VMWriter;
import codegen.VMWriter.Segment;
import codegen.VMWriter.Command;

public class GrammarStatements {

    private final Parser parser;
    private final SymbolTable symbolTable;
    private final VMWriter vmWriter;
    private final GrammarExpressionsTerms expressions;
    private int labelIndex = 0; // Contador para gerar rótulos únicos (IF_TRUE0, IF_FALSE0, WHILE_EXP0, etc.)
    private String currentClassName;

    public GrammarStatements(Parser parser, SymbolTable symbolTable, VMWriter vmWriter) {
        if (parser == null || symbolTable == null || vmWriter == null) {
            throw new IllegalArgumentException("Nenhuma dependência pode ser nula.");
        }

        this.parser = parser;
        this.symbolTable = symbolTable;
        this.vmWriter = vmWriter;
        this.expressions = new GrammarExpressionsTerms(parser, symbolTable, vmWriter);
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
                    throw new IllegalStateException("Comando não reconhecido: " + command);
            }
        }
    }

    public void compileLet() {
        consumeKeyword("let");
        Token varToken = parser.consume(TokenType.IDENTIFIER);
        String varName = varToken.getValue();

        boolean isArray = false;

        if (checkSymbol("[")) {
            isArray = true;
            // 1. Empilha o endereço base do array
            vmWriter.writePush(convertKindToSegment(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));
            consumeSymbol("[");
            expressions.compileExpression(); // Empilha o índice i
            consumeSymbol("]");
            // Endereço alvo = base + i
            vmWriter.writeArithmetic(Command.ADD);
        }

        consumeSymbol("=");
        expressions.compileExpression(); // Compila a expressão da direita
        consumeSymbol(";");

        if (isArray) {
            // Salva o resultado da expressão em temp 0
            vmWriter.writePop(Segment.TEMP, 0);
            // Salva o endereço de destino (base + i) em pointer 1 (THAT)
            vmWriter.writePop(Segment.POINTER, 1);
            // Empilha o resultado salvo em temp 0
            vmWriter.writePush(Segment.TEMP, 0);
            // Salva o resultado na posição do array (*THAT)
            vmWriter.writePop(Segment.THAT, 0);
        } else {
            // Atribuição de variável simples
            Segment segment = convertKindToSegment(symbolTable.kindOf(varName));
            int index = symbolTable.indexOf(varName);
            vmWriter.writePop(segment, index);
        }
    }

    public void compileIf() {
        int labelNum = labelIndex++;
        String labelTrue = "IF_TRUE" + labelNum;
        String labelFalse = "IF_FALSE" + labelNum;
        String labelEnd = "IF_END" + labelNum;

        consumeKeyword("if");
        consumeSymbol("(");
        expressions.compileExpression(); // Avalia a condição do if
        consumeSymbol(")");

        vmWriter.writeIf(labelTrue);
        vmWriter.writeGoto(labelFalse);
        vmWriter.writeLabel(labelTrue);

        consumeSymbol("{");
        compileStatements(); // Bloco 'if'
        consumeSymbol("}");

        if (checkKeyword("else")) {
            vmWriter.writeGoto(labelEnd);
            vmWriter.writeLabel(labelFalse);

            consumeKeyword("else");
            consumeSymbol("{");
            compileStatements(); // Bloco 'else'
            consumeSymbol("}");

            vmWriter.writeLabel(labelEnd);
        } else {
            vmWriter.writeLabel(labelFalse);
        }
    }

    public void compileWhile() {
        int labelNum = labelIndex++;
        String labelExp = "WHILE_EXP" + labelNum;
        String labelEnd = "WHILE_END" + labelNum;

        vmWriter.writeLabel(labelExp);

        consumeKeyword("while");
        consumeSymbol("(");
        expressions.compileExpression(); // Avalia a condição do loop
        consumeSymbol(")");

        vmWriter.writeArithmetic(Command.NOT); // Inverte a condição
        vmWriter.writeIf(labelEnd);            // Sai do loop se a condição for falsa

        consumeSymbol("{");
        compileStatements(); // Corpo do while
        consumeSymbol("}");

        vmWriter.writeGoto(labelExp);
        vmWriter.writeLabel(labelEnd);
    }

    public void compileDo() {
        consumeKeyword("do");
        
        // compileIdentifierTerm ou chamada de função tratada no GrammarExpressionsTerms
        Token firstNameToken = parser.consume(TokenType.IDENTIFIER);
        compileSubroutineCall(firstNameToken.getValue());
        
        consumeSymbol(";");

        // Em comandos 'do', o valor retornado pela função deve ser descartado salvando em temp 0
        vmWriter.writePop(Segment.TEMP, 0);
    }

    public void compileReturn() {
        consumeKeyword("return");

        if (!checkSymbol(";")) {
            expressions.compileExpression();
        } else {
            // Funções void na linguagem Jack sempre devem retornar a constante 0 na VM
            vmWriter.writePush(Segment.CONST, 0);
        }

        consumeSymbol(";");
        vmWriter.writeReturn();
    }

    public void setCurrentClassName(String currentClassName) {
        this.currentClassName = currentClassName;
        this.expressions.setCurrentClassName(currentClassName);
    }

    private void compileSubroutineCall(String firstName) {
        String subroutineName = firstName;
        int nArgs = 0;

        if (checkSymbol(".")) {
            consumeSymbol(".");
            Token methodToken = parser.consume(TokenType.IDENTIFIER);
            String objectOrClass = firstName;
            String methodName = methodToken.getValue();

            SymbolTable.Kind kind = symbolTable.kindOf(objectOrClass);

            if (kind != SymbolTable.Kind.NONE) {
                vmWriter.writePush(convertKindToSegment(kind), symbolTable.indexOf(objectOrClass));
                subroutineName = symbolTable.typeOf(objectOrClass) + "." + methodName;
                nArgs = 1;
            } else {
                subroutineName = objectOrClass + "." + methodName;
            }
        } else {
            // Chamada de método da própria classe
            vmWriter.writePush(Segment.POINTER, 0);
            subroutineName = currentClassName + "." + firstName; // <-- ATUALIZADO
            nArgs = 1;
        }

        consumeSymbol("(");
        nArgs += expressions.compileExpressionList();
        consumeSymbol(")");

        vmWriter.writeCall(subroutineName, nArgs);
    }

    private boolean checkKeyword(String expected) {
        Token token = parser.peek();
        return token != null && token.getType() == TokenType.KEYWORD && token.getValue().equals(expected);
    }

    private boolean checkSymbol(String expected) {
        Token token = parser.peek();
        return token != null && token.getType() == TokenType.SYMBOL && token.getValue().equals(expected);
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

    private Segment convertKindToSegment(SymbolTable.Kind kind) {
        switch (kind) {
            case STATIC: return Segment.STATIC;
            case FIELD:  return Segment.THIS;
            case ARG:    return Segment.ARG;
            case VAR:    return Segment.LOCAL;
            default:     throw new IllegalArgumentException("Kind não mapeado na SymbolTable: " + kind);
        }
    }

    private RuntimeException unexpected(String expected) {
        Token token = parser.peek();
        if (token == null) {
            return new RuntimeException("Erro de sintaxe: esperado '" + expected + "', mas o arquivo terminou.");
        }
        return new RuntimeException("Erro de sintaxe na linha " + token.getLine()
                + ", coluna " + token.getColumn()
                + ": esperado '" + expected + "', mas encontrado '" + token.getValue() + "'.");
    }
}