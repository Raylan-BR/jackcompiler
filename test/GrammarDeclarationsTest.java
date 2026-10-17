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

    // Testes para verificar a aceitação de uma classe vazia.
    @Test
    void shouldAcceptEmptyClass() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "class"),
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),
            token(TokenType.SYMBOL, "}")
        );

        assertDoesNotThrow(grammar::compileClass);
        assertTrue(grammar.isAtEnd());
    }

    // Testes para verificar a aceitação de declarações de variáveis e sub-rotinas.
    @Test
    void shouldAcceptStaticAndFieldDeclarations() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "class"),
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),

            token(TokenType.KEYWORD, "field"),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "x"),
            token(TokenType.SYMBOL, ","),
            token(TokenType.IDENTIFIER, "y"),
            token(TokenType.SYMBOL, ";"),

            token(TokenType.KEYWORD, "static"),
            token(TokenType.KEYWORD, "boolean"),
            token(TokenType.IDENTIFIER, "ativo"),
            token(TokenType.SYMBOL, ";"),

            token(TokenType.SYMBOL, "}")
        );

        assertDoesNotThrow(grammar::compileClass);
        assertTrue(grammar.isAtEnd());
    }

    // Testes para verificar a aceitação de sub-rotinas com e sem parâmetros.
    @Test
    void shouldAcceptSubroutineWithoutParameters() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "class"),
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),

            token(TokenType.KEYWORD, "function"),
            token(TokenType.KEYWORD, "void"),
            token(TokenType.IDENTIFIER, "iniciar"),
            token(TokenType.SYMBOL, "("),
            token(TokenType.SYMBOL, ")"),
            token(TokenType.SYMBOL, "{"),
            token(TokenType.SYMBOL, "}"),

            token(TokenType.SYMBOL, "}")
        );

        assertDoesNotThrow(grammar::compileClass);
        assertTrue(grammar.isAtEnd());
    }

    // Testes para verificar a aceitação de sub-rotinas com múltiplos parâmetros.
    @Test
    void shouldAcceptSubroutineWithMultipleParameters() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "class"),
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),

            token(TokenType.KEYWORD, "function"),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "somar"),
            token(TokenType.SYMBOL, "("),

            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "a"),
            token(TokenType.SYMBOL, ","),

            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "b"),
            token(TokenType.SYMBOL, ")"),

            token(TokenType.SYMBOL, "{"),
            token(TokenType.KEYWORD, "var"),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "resultado"),
            token(TokenType.SYMBOL, ";"),
            token(TokenType.SYMBOL, "}"),

            token(TokenType.SYMBOL, "}")
        );

        assertDoesNotThrow(grammar::compileClass);
        assertTrue(grammar.isAtEnd());
    }

    // Testes para verificar a rejeição de classes sem a palavra-chave "class".
    @Test
    void shouldRejectClassWithoutClassKeyword() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),
            token(TokenType.SYMBOL, "}")
        );

        assertThrows(
            RuntimeException.class,
            grammar::compileClass
        );
    }

    // Testes para verificar a rejeição de declarações de variáveis sem nome.
    @Test
    void shouldRejectVariableDeclarationWithoutName() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "class"),
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),

            token(TokenType.KEYWORD, "field"),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.SYMBOL, ";"),

            token(TokenType.SYMBOL, "}")
        );

        assertThrows(
            RuntimeException.class,
            grammar::compileClass
        );
    }

    // Testes para verificar a rejeição de listas de parâmetros sem vírgula entre eles.
    @Test
    void shouldRejectParameterListWithoutComma() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "class"),
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),

            token(TokenType.KEYWORD, "function"),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "somar"),
            token(TokenType.SYMBOL, "("),

            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "a"),
            token(TokenType.KEYWORD, "int"),
            token(TokenType.IDENTIFIER, "b"),

            token(TokenType.SYMBOL, ")"),
            token(TokenType.SYMBOL, "{"),
            token(TokenType.SYMBOL, "}"),
            token(TokenType.SYMBOL, "}")
        );

        assertThrows(
            RuntimeException.class,
            grammar::compileClass
        );
    }

    // Testes para verificar a rejeição de sub-rotinas sem a chave de fechamento.
    @Test
    void shouldRejectSubroutineWithoutClosingBrace() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "class"),
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),

            token(TokenType.KEYWORD, "function"),
            token(TokenType.KEYWORD, "void"),
            token(TokenType.IDENTIFIER, "iniciar"),
            token(TokenType.SYMBOL, "("),
            token(TokenType.SYMBOL, ")"),
            token(TokenType.SYMBOL, "{"),

            token(TokenType.SYMBOL, "}")
        );

        assertThrows(
            RuntimeException.class,
            grammar::compileClass
        );
    }

    // Testes para verificar a rejeição de tokens adicionais após a declaração da classe.
    @Test
    void shouldRejectTokensAfterClassDeclaration() {
        GrammarDeclarations grammar = createGrammar(
            token(TokenType.KEYWORD, "class"),
            token(TokenType.IDENTIFIER, "Main"),
            token(TokenType.SYMBOL, "{"),
            token(TokenType.SYMBOL, "}"),
            token(TokenType.IDENTIFIER, "extra")
        );

        assertThrows(
            RuntimeException.class,
            grammar::compileClass
        );
    }
}
