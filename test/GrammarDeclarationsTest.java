import java.util.List;

public class GrammarDeclarationsTest {

    public static void main(String[] args) {

        List<Token> tokens = List.of(
            new Token(TokenType.KEYWORD, "class", 0, 0),
            new Token(TokenType.IDENTIFIER, "Main", 0, 0)
        );

        Parser parser = new Parser(tokens);
        GrammarDeclarations grammar = new GrammarDeclarations(parser);

        // Teste 1: consulta o primeiro token.
        if (!grammar.peek().getValue().equals("class")) {
            throw new AssertionError(
                "Falha ao consultar o token atual."
            );
        }

        System.out.println("PASSOU: consulta do token atual");

        // Teste 2: consome o primeiro token.
        Token primeiro = grammar.advance();

        if (!primeiro.getValue().equals("class")) {
            throw new AssertionError(
                "Falha ao consumir o primeiro token."
            );
        }

        // Confirma que o próximo token é Main.
        if (!grammar.peek().getValue().equals("Main")) {
            throw new AssertionError(
                "Falha ao avançar para o próximo token."
            );
        }

        System.out.println("PASSOU: avanço entre tokens");

        // Teste 3: consome o token esperado.
        Token segundo = grammar.consume(TokenType.IDENTIFIER);

        if (!segundo.getValue().equals("Main")) {
            throw new AssertionError(
                "Falha ao consumir o identificador."
            );
        }

        if (!grammar.isAtEnd()) {
            throw new AssertionError(
                "O parser deveria estar no fim dos tokens."
            );
        }

        System.out.println("PASSOU: consumo e fim dos tokens");

        // Teste 4: tentativa de consumir além do fim.
        try {
            grammar.consume(TokenType.KEYWORD);

            throw new AssertionError(
                "Era esperado um erro de sintaxe."
            );

        } catch (RuntimeException e) {
            System.out.println(
                "PASSOU: tratamento de fim inesperado"
            );
        }

        System.out.println("Todos os testes passaram!");
    }
}