import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

public class GrammarDeclarationsTest {

    private Token token(TokenType type, String value) {
        return new Token(type, value, 1, 1);
    }

    private GrammarDeclarations createGrammar(Token... tokens) {
        Parser parser = new Parser(List.of(tokens));
        return new GrammarDeclarations(parser);
    }

    @Test
    void shouldAcceptEmptyClass() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "class"),
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),
            token(TokenType.SYMBOL, "}")
        );

        assertDoesNotThrow(grammar::compile_class);
        assertTrue(grammar.isAtEnd());
    }

    @Test
    void shouldRejectClassWithoutClassKeyword() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),
            token(TokenType.SYMBOL, "}")
        );

        assertThrows(
            RuntimeException.class,
            grammar::compile_class
        );
    }

    @Test
    void shouldAcceptFieldDeclaration() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "field"),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "x"),
            token(TokenType.SYMBOL, ";")
        );

        assertDoesNotThrow(grammar::compile_class_var_dec);
        assertTrue(grammar.isAtEnd());
    }

    @Test
    void shouldAcceptCommaSeparatedVariables() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "static"),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "x"),
            token(TokenType.SYMBOL, ","),
            token(TokenType.IDENTIFIER, "y"),
            token(TokenType.SYMBOL, ";")
        );

        assertDoesNotThrow(grammar::compile_class_var_dec);
        assertTrue(grammar.isAtEnd());
    }

    @Test
    void shouldRejectDeclarationWithoutVariableName() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "field"),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.SYMBOL, ";")
        );

        assertThrows(
            RuntimeException.class,
            grammar::compile_class_var_dec
        );
    }

    @Test
    void shouldAcceptFunctionWithoutParameters() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "function"),
            token(TokenType.KEYWORD, "void"),
            token(TokenType.IDENTIFIER, "reset"),
            token(TokenType.SYMBOL, "("),
            token(TokenType.SYMBOL, ")"),
            token(TokenType.SYMBOL, "{"),
            token(TokenType.SYMBOL, "}")
        );

        assertDoesNotThrow(grammar::compile_subroutine);
        assertTrue(grammar.isAtEnd());
    }

    @Test
    void shouldAcceptMethodWithParameters() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "method"),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "sum"),
            token(TokenType.SYMBOL, "("),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "a"),
            token(TokenType.SYMBOL, ","),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "b"),
            token(TokenType.SYMBOL, ")"),
            token(TokenType.SYMBOL, "{"),
            token(TokenType.SYMBOL, "}")
        );

        assertDoesNotThrow(grammar::compile_subroutine);
        assertTrue(grammar.isAtEnd());
    }

    @Test
    void shouldRejectSubroutineWithoutName() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "function"),
            token(TokenType.KEYWORD, "void"),
            token(TokenType.SYMBOL, "("),
            token(TokenType.SYMBOL, ")"),
            token(TokenType.SYMBOL, "{"),
            token(TokenType.SYMBOL, "}")
        );

        assertThrows(
            RuntimeException.class,
            grammar::compile_subroutine
        );
    }

    @Test
    void shouldRejectSubroutineWithoutClosingBodyBrace() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "function"),
            token(TokenType.KEYWORD, "void"),
            token(TokenType.IDENTIFIER, "reset"),
            token(TokenType.SYMBOL, "("),
            token(TokenType.SYMBOL, ")"),
            token(TokenType.SYMBOL, "{")
        );

        assertThrows(
            RuntimeException.class,
            grammar::compile_subroutine
        );
    }
}