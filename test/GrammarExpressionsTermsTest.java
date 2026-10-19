import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class GrammarExpressionsTermsTest {

    @Test
    void deveAceitarExpressaoValida() {
        List<Token> tokens = List.of(
            new Token(TokenType.IDENTIFIER, "x", 1, 1),
            new Token(TokenType.SYMBOL, "+", 1, 3),
            new Token(TokenType.INTEGER_CONSTANT, "2", 1, 5)
        );

        Parser parser = new Parser(tokens);
        GrammarExpressionsTerms grammar =
                new GrammarExpressionsTerms(parser);

        assertDoesNotThrow(() -> grammar.compileExpression());

        assertTrue(parser.isAtEnd());
    }
    @Test
    void deveRejeitarExpressaoInvalida() {
        List<Token> tokens = List.of(
            new Token(TokenType.IDENTIFIER, "x", 1, 1),
            new Token(TokenType.SYMBOL, "+", 1, 3)
        );

        Parser parser = new Parser(tokens);
        GrammarExpressionsTerms grammar =
                new GrammarExpressionsTerms(parser);

        assertThrows(
            RuntimeException.class,
            () -> grammar.compileExpression()
        );
    }
}