import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import lexer.Lexer;
import parser.GrammarExpressionsTerms;
import parser.Parser;
import writer.SyntaxXmlWriter;

public class GrammarExpressionsTermsTest {

    private Parser parser;

    private GrammarExpressionsTerms grammarOf(String code) {
        SyntaxXmlWriter writer = new SyntaxXmlWriter(new StringWriter());
        parser = new Parser(new Lexer(code).tokenize(), writer);
        return new GrammarExpressionsTerms(parser, writer);
    }

    @Test
    void deveAceitarExpressaoValida() {
        GrammarExpressionsTerms grammar = grammarOf("x + 2");

        assertDoesNotThrow(grammar::compileExpression);
        assertTrue(parser.isAtEnd());
    }

    @Test
    void deveRejeitarExpressaoInvalida() {
        GrammarExpressionsTerms grammar = grammarOf("x +");

        assertThrows(RuntimeException.class, grammar::compileExpression);
    }

    @Test
    void deveAceitarExpressaoComParenteses() {
        GrammarExpressionsTerms grammar = grammarOf("(x + 1) * 2");

        assertDoesNotThrow(grammar::compileExpression);
        assertTrue(parser.isAtEnd());
    }

    @Test
    void deveAceitarOperadorUnario() {
        GrammarExpressionsTerms grammar = grammarOf("~x");

        assertDoesNotThrow(grammar::compileExpression);
        assertTrue(parser.isAtEnd());
    }

    @Test
    void deveAceitarAcessoAArray() {
        GrammarExpressionsTerms grammar = grammarOf("a[i + 1]");

        assertDoesNotThrow(grammar::compileExpression);
        assertTrue(parser.isAtEnd());
    }

    @Test
    void deveAceitarChamadaDeSubrotina() {
        GrammarExpressionsTerms grammar = grammarOf("Math.max(a, 2)");

        assertDoesNotThrow(grammar::compileExpression);
        assertTrue(parser.isAtEnd());
    }

    @Test
    void deveRejeitarTermoInvalido() {
        GrammarExpressionsTerms grammar = grammarOf(")");

        assertThrows(RuntimeException.class, grammar::compileExpression);
    }
}