import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GrammarStatementsTest {

    private static GrammarStatements grammarOf(String code) {
        return new GrammarStatements(new Lexer(code).tokenize());
    }

    // ---------- identifyNextCommand ----------

    @Test
    void deveIdentificarCadaComando() {
        assertEquals("let", grammarOf("let x = 1;").identifyNextCommand());
        assertEquals("if", grammarOf("if (x) { }").identifyNextCommand());
        assertEquals("while", grammarOf("while (x) { }").identifyNextCommand());
        assertEquals("do", grammarOf("do foo();").identifyNextCommand());
        assertEquals("return", grammarOf("return;").identifyNextCommand());
    }

    @Test
    void naoDeveIdentificarTokenQueNaoEhComando() {
        assertNull(grammarOf("}").identifyNextCommand());
        assertNull(grammarOf("x").identifyNextCommand());
        assertNull(grammarOf("").identifyNextCommand());
    }

    @Test
    void naoDeveConfundirStringComComando() {
        assertNull(grammarOf("\"let\"").identifyNextCommand());
    }

    // ---------- comandos isolados ----------

    @Test
    void deveCompilarLetSimples() {
        GrammarStatements g = grammarOf("let x = 5;");
        g.compileLet();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarLetComExpressao() {
        GrammarStatements g = grammarOf("let x = (a + 2) * -b;");
        g.compileLet();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarLetComIndice() {
        GrammarStatements g = grammarOf("let a[i + 1] = b[2];");
        g.compileLet();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarIfSemElse() {
        GrammarStatements g = grammarOf("if (x < 3) { let y = 1; }");
        g.compileIf();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarIfComElse() {
        GrammarStatements g = grammarOf(
            "if (x) { let y = 1; } else { let y = 2; }");
        g.compileIf();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarIfComCorpoVazio() {
        GrammarStatements g = grammarOf("if (x) { } else { }");
        g.compileIf();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarWhile() {
        GrammarStatements g = grammarOf("while (i < 10) { let i = i + 1; }");
        g.compileWhile();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarDoComFuncaoLocal() {
        GrammarStatements g = grammarOf("do draw();");
        g.compileDo();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarDoComMetodoEArgumentos() {
        GrammarStatements g = grammarOf("do Output.printInt(a, b + 1);");
        g.compileDo();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarReturnSemValor() {
        GrammarStatements g = grammarOf("return;");
        g.compileReturn();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarReturnComValor() {
        GrammarStatements g = grammarOf("return x + 1;");
        g.compileReturn();
        assertTrue(g.getParser().isAtEnd());
    }

    // ---------- sequências e aninhamento ----------

    @Test
    void deveCompilarSequenciaDeComandos() {
        GrammarStatements g = grammarOf(
            "let x = 1; do foo(); return x;");
        g.compileStatements();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveAceitarListaDeComandosVazia() {
        GrammarStatements g = grammarOf("");
        g.compileStatements();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void devePararNoFechamentoDeBloco() {
        GrammarStatements g = grammarOf("let x = 1; }");
        g.compileStatements();

        assertFalse(g.getParser().isAtEnd());
        assertEquals("}", g.getParser().peek().getValue());
    }

    @Test
    void deveCompilarIfDentroDeWhile() {
        GrammarStatements g = grammarOf(
            "while (x) {"
            + "  if (y) { return; }"
            + "  let x = x - 1;"
            + "}");
        g.compileWhile();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarWhileDentroDeIfComElse() {
        GrammarStatements g = grammarOf(
            "if (a) { while (b) { do foo(); } } else { let x = 0; }");
        g.compileIf();
        assertTrue(g.getParser().isAtEnd());
    }

    @Test
    void deveCompilarAninhamentoProfundo() {
        GrammarStatements g = grammarOf(
            "while (a) { while (b) { if (c) { if (d) { return; } } } }");
        g.compileStatements();
        assertTrue(g.getParser().isAtEnd());
    }

    // ---------- erros de sintaxe ----------

    @Test
    void deveFalharLetSemPontoEVirgula() {
        GrammarStatements g = grammarOf("let x = 5");

        RuntimeException ex = assertThrows(RuntimeException.class, g::compileLet);
        assertTrue(ex.getMessage().contains("o arquivo terminou"));
    }

    @Test
    void deveFalharLetSemIgual() {
        GrammarStatements g = grammarOf("let x 5;");

        RuntimeException ex = assertThrows(RuntimeException.class, g::compileLet);
        assertTrue(ex.getMessage().contains("esperado '='"));
    }

    @Test
    void deveFalharIfSemParenteses() {
        GrammarStatements g = grammarOf("if x) { }");

        RuntimeException ex = assertThrows(RuntimeException.class, g::compileIf);
        assertTrue(ex.getMessage().contains("Erro de sintaxe"));
    }

    @Test
    void deveFalharWhileSemChaveDeFechamento() {
        GrammarStatements g = grammarOf("while (x) { return;");

        assertThrows(RuntimeException.class, g::compileWhile);
    }

    @Test
    void deveFalharElseSemChaves() {
        GrammarStatements g = grammarOf("if (x) { } else return;");

        assertThrows(RuntimeException.class, g::compileIf);
    }

    @Test
    void deveFalharDoSemParenteses() {
        GrammarStatements g = grammarOf("do foo;");

        assertThrows(RuntimeException.class, g::compileDo);
    }

    @Test
    void deveFalharReturnSemPontoEVirgula() {
        GrammarStatements g = grammarOf("return x");

        assertThrows(RuntimeException.class, g::compileReturn);
    }

    @Test
    void deveFalharQuandoComandoNaoComecaComSuaPalavraChave() {
        GrammarStatements g = grammarOf("if (x) { }");

        assertThrows(RuntimeException.class, g::compileWhile);
    }
}