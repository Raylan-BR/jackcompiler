public class GrammarExpressionsTerms {

    private final Parser parser;

    public GrammarExpressionsTerms(Parser parser){
        if(parser == null){
            throw new IllegalArgumentException(
                "O parser não pode ser nulo."
            );
        }

        this.parser = parser;

    }

    public void compileExpression(){
        compileTerm();

        while(compileOp()){
            compileTerm();
        }

    }

    public void compileTerm(){
        Token token = parser.peek();

        if(token == null){
            throw new RuntimeException(
                "Erro de sintaxe: esperado um termo, mas o arquivo terminou."
            );
        }

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
                if(compileUnaryOp()){
                    compileTerm();
                } else if(isSymbol("(")){
                    // exemplo: (x + 2)
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
    }

    public boolean compileOp(){
        Token token = parser.peek();

        if(token == null || token.getType() != TokenType.SYMBOL){
            return false;
        }

        String operator = token.getValue();

        switch (operator) {
            case "+","-","*","/","&","|","<",">","=":
                parser.advance();
                return true;
        
            default:
                return false;
        }
    }
    
    public boolean compileUnaryOp(){
        Token token = parser.peek();

        if(token == null || token.getType() != TokenType.SYMBOL){
            return false;
        }

        String operator = token.getValue();

        if(operator.equals("-") || operator.equals("~")){
            parser.advance();
            return true;
        }

        return false;
    }

    public boolean compileKeywordConstant(){
        Token token = parser.peek();

        if (token == null || token.getType() != TokenType.KEYWORD) {
            return false;
        }

        String keyword = token.getValue();

        switch (keyword) {
            case "true", "false", "null", "this":
                parser.advance();
                return true;
        
            default:
                return false;
        }
    }

    // métodos auxiliares
    
    private boolean isSymbol(String expected){
        Token token = parser.peek();

        return token != null
            && token.getType() == TokenType.SYMBOL
            && token.getValue().equals((expected));
    }

    private void consumeSymbol(String expected){
        if(!isSymbol(expected)){
            Token token = parser.peek();

            if(token == null){
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

    private void compileExpressionList(){
        // uma lista vazia é válida.
        if(isSymbol(")")){
            return;
        }
        compileExpression();

        while(isSymbol(",")){
            consumeSymbol(",");
            compileExpression();
        }
    }

    private void compileIdentifierTerm(){
        parser.consume(TokenType.IDENTIFIER);

        if(isSymbol("[")){
            // caso: vector[i]
            consumeSymbol("[");
            compileExpression();
            consumeSymbol("]");
        } else if(isSymbol("(")){
            // caso: function(x)
            consumeSymbol("(");
            compileExpressionList();
            consumeSymbol(")");
        } else if(isSymbol(".")){
            // caso: object.method(x)
            consumeSymbol(".");
            parser.consume(TokenType.IDENTIFIER);
            consumeSymbol("(");
            compileExpressionList();
            consumeSymbol(")");
        }

        // se não houver [, ( ou ., é uma variável simples
    }
}
