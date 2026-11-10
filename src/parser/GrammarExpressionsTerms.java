package parser;

import lexer.Token;
import lexer.TokenType;
import codegen.SymbolTable;
import codegen.VMWriter;
import codegen.VMWriter.Segment;
import codegen.VMWriter.Command;

public class GrammarExpressionsTerms {

    private final Parser parser;
    private final SymbolTable symbolTable;
    private final VMWriter vmWriter;

    public GrammarExpressionsTerms(Parser parser, SymbolTable symbolTable, VMWriter vmWriter) {
        if (parser == null || symbolTable == null || vmWriter == null) {
            throw new IllegalArgumentException("Nenhuma dependência pode ser nula.");
        }

        this.parser = parser;
        this.symbolTable = symbolTable;
        this.vmWriter = vmWriter;
    }

    /**
     * Compila uma expressão utilizando a Notação Pós-Fixada (RPN).
     * Exemplo: 'a + b' gera: push a, push b, add
     */
    public void compileExpression() {
        compileTerm();

        while (isBinaryOp()) {
            Token opToken = parser.advance();

            // Compila o termo à direita do operador
            compileTerm();

            // Emite o comando VM correspondente ao operador
            compileOp(opToken.getValue());
        }
    }

    /**
     * Compila um termo individual (constantes, variáveis, chamadas de subrotina, sub-expressões).
     */
    public void compileTerm() {
        Token token = parser.peek();

        if (token == null) {
            throw new RuntimeException("Erro de sintaxe: esperado um termo, mas o arquivo terminou.");
        }

        switch (token.getType()) {
            case INTEGER_CONSTANT:
                int val = Integer.parseInt(token.getValue());
                vmWriter.writePush(Segment.CONST, val);
                parser.advance();
                break;

            case STRING_CONSTANT:
                compileStringConstant(token.getValue());
                parser.advance();
                break;

            case KEYWORD:
                compileKeywordConstant(token.getValue());
                parser.advance();
                break;

            case IDENTIFIER:
                compileIdentifierTerm();
                break;

            case SYMBOL:
                if (isUnaryOp(token.getValue())) {
                    String op = token.getValue();
                    parser.advance();
                    compileTerm();
                    
                    if (op.equals("-")) {
                        vmWriter.writeArithmetic(Command.NEG);
                    } else if (op.equals("~")) {
                        vmWriter.writeArithmetic(Command.NOT);
                    }
                } else if (isSymbol("(")) {
                    consumeSymbol("(");
                    compileExpression();
                    consumeSymbol(")");
                } else {
                    throw syntaxError("Termo símbolo inválido: '" + token.getValue() + "'");
                }
                break;

            default:
                throw syntaxError("Termo inválido: '" + token.getValue() + "'");
        }
    }

    /**
     * Trata identificadores: variáveis simples, elementos de array 'x[i]' e chamadas de método/função.
     */
    private void compileIdentifierTerm() {
        Token nameToken = parser.consume(TokenType.IDENTIFIER);
        String name = nameToken.getValue();

        if (isSymbol("[")) {
            // Acesso a Array: name[expr]
            vmWriter.writePush(convertKindToSegment(symbolTable.kindOf(name)), symbolTable.indexOf(name));
            consumeSymbol("[");
            compileExpression();
            consumeSymbol("]");

            vmWriter.writeArithmetic(Command.ADD); // Endereço = base + índice
            vmWriter.writePop(Segment.POINTER, 1);  // Aponta THAT para o endereço
            vmWriter.writePush(Segment.THAT, 0);   // Lê o valor da posição do array

        } else if (isSymbol("(") || isSymbol(".")) {
            // Chamada de Subrotina
            compileSubroutineCall(name);

        } else {
            // Variável simples
            SymbolTable.Kind kind = symbolTable.kindOf(name);
            int index = symbolTable.indexOf(name);
            vmWriter.writePush(convertKindToSegment(kind), index);
        }
    }

    /**
     * Compila a lista de argumentos de uma função e retorna a quantidade de argumentos compilados.
     */
    public int compileExpressionList() {
        int nArgs = 0;

        if (!isSymbol(")")) {
            compileExpression();
            nArgs++;

            while (isSymbol(",")) {
                consumeSymbol(",");
                compileExpression();
                nArgs++;
            }
        }

        return nArgs;
    }

    /**
     * Compila chamadas de subrotina no formato: 'metodo(arg)' ou 'Objeto.metodo(arg)'
     */
    private void compileSubroutineCall(String firstName) {
        String subroutineName = firstName;
        int nArgs = 0;

        if (isSymbol(".")) {
            consumeSymbol(".");
            Token methodToken = parser.consume(TokenType.IDENTIFIER);
            String objectOrClass = firstName;
            String methodName = methodToken.getValue();

            SymbolTable.Kind kind = symbolTable.kindOf(objectOrClass);

            if (kind != SymbolTable.Kind.NONE) {
                // É um método chamado sobre uma instância de objeto (ex: game.run())
                vmWriter.writePush(convertKindToSegment(kind), symbolTable.indexOf(objectOrClass));
                subroutineName = symbolTable.typeOf(objectOrClass) + "." + methodName;
                nArgs = 1; // O próprio objeto é passado como primeiro argumento (this)
            } else {
                // É uma função estática ou construtor (ex: Memory.peek(100))
                subroutineName = objectOrClass + "." + methodName;
            }
        } else {
            // Método da própria classe atual (ex: redraw())
            vmWriter.writePush(Segment.POINTER, 0); // Passa 'this' como argumento 0
            nArgs = 1;
        }

        consumeSymbol("(");
        nArgs += compileExpressionList();
        consumeSymbol(")");

        vmWriter.writeCall(subroutineName, nArgs);
    }

    /**
     * Compila a criação de String na VM usando a biblioteca OS do Nand2Tetris (String.new e String.appendChar).
     */
    private void compileStringConstant(String str) {
        vmWriter.writePush(Segment.CONST, str.length());
        vmWriter.writeCall("String.new", 1);

        for (char c : str.toCharArray()) {
            vmWriter.writePush(Segment.CONST, (int) c);
            vmWriter.writeCall("String.appendChar", 2);
        }
    }

    /**
     * Mapeia as palavras-chave constantes para a VM (true, false, null, this).
     */
    private void compileKeywordConstant(String keyword) {
        switch (keyword) {
            case "true":
                // Em Jack, true é representado por -1 (push 0 followed by not)
                vmWriter.writePush(Segment.CONST, 0);
                vmWriter.writeArithmetic(Command.NOT);
                break;

            case "false":
            case "null":
                vmWriter.writePush(Segment.CONST, 0);
                break;

            case "this":
                vmWriter.writePush(Segment.POINTER, 0);
                break;

            default:
                throw syntaxError("Palavra-chave constante desconhecida: " + keyword);
        }
    }

    /**
     * Mapeia os operadores binários para comandos da VM ou chamadas de biblioteca OS.
     */
    private void compileOp(String op) {
        switch (op) {
            case "+": vmWriter.writeArithmetic(Command.ADD); break;
            case "-": vmWriter.writeArithmetic(Command.SUB); break;
            case "=": vmWriter.writeArithmetic(Command.EQ); break;
            case ">": vmWriter.writeArithmetic(Command.GT); break;
            case "<": vmWriter.writeArithmetic(Command.LT); break;
            case "&": vmWriter.writeArithmetic(Command.AND); break;
            case "|": vmWriter.writeArithmetic(Command.OR); break;
            case "*": vmWriter.writeCall("Math.multiply", 2); break;
            case "/": vmWriter.writeCall("Math.divide", 2); break;
            default:  throw syntaxError("Operador binário desconhecido: " + op);
        }
    }

    private boolean isBinaryOp() {
        Token token = parser.peek();
        if (token == null || token.getType() != TokenType.SYMBOL) return false;
        String val = token.getValue();
        return val.equals("+") || val.equals("-") || val.equals("*") || val.equals("/")
            || val.equals("&") || val.equals("|") || val.equals("<") || val.equals(">") || val.equals("=");
    }

    private boolean isUnaryOp(String op) {
        return op.equals("-") || op.equals("~");
    }

    private boolean isSymbol(String expected) {
        Token token = parser.peek();
        return token != null && token.getType() == TokenType.SYMBOL && token.getValue().equals(expected);
    }

    private void consumeSymbol(String expected) {
        if (!isSymbol(expected)) {
            throw syntaxError("Esperado símbolo '" + expected + "'");
        }
        parser.advance();
    }

    private VMWriter.Segment convertKindToSegment(SymbolTable.Kind kind) {
        switch (kind) {
            case STATIC: return Segment.STATIC;
            case FIELD:  return Segment.THIS;
            case ARG:    return Segment.ARG;
            case VAR:    return Segment.LOCAL;
            default:     throw new IllegalArgumentException("Kind não mapeado: " + kind);
        }
    }

    private RuntimeException syntaxError(String message) {
        Token token = parser.peek();
        String info = token != null ? " no token '" + token.getValue() + "' (linha " + token.getLine() + ")" : " no fim da entrada";
        return new RuntimeException("Erro de sintaxe" + info + ": " + message);
    }
}