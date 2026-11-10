import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

public class LexerTest {

    // ---------- auxiliares ----------

    private static List<Token> tokenize(String input) {
        return new Lexer(input).tokenize();
    }

    private static void assertSingleToken(
            String input, TokenType type, String value) {

        List<Token> tokens = tokenize(input);

        assertEquals(1, tokens.size(), "Entrada: " + input);
        assertEquals(type, tokens.get(0).getType(), "Entrada: " + input);
        assertEquals(value, tokens.get(0).getValue(), "Entrada: " + input);
    }

    private static void assertTokenValues(
            String input, String... expected) {

        List<String> actual = tokenize(input).stream()
            .map(Token::getValue)
            .toList();

        assertEquals(List.of(expected), actual);
    }

    private static String toXmlTokenType(TokenType type) {
        switch (type) {
            case KEYWORD:
                return "keyword";
            case SYMBOL:
                return "symbol";
            case INTEGER_CONSTANT:
                return "integerConstant";
            case STRING_CONSTANT:
                return "stringConstant";
            case IDENTIFIER:
                return "identifier";
            default:
                throw new IllegalArgumentException(
                    "Tipo de token desconhecido: " + type);
        }
    }

    // ---------- tokens básicos ----------

    @Test
    void deveReconhecerPalavrasChave() {
        assertSingleToken("class", TokenType.KEYWORD, "class");
        assertSingleToken("function", TokenType.KEYWORD, "function");
        assertSingleToken("return", TokenType.KEYWORD, "return");
    }

    @Test
    void deveReconhecerIdentificadores() {
        assertSingleToken("Main", TokenType.IDENTIFIER, "Main");
        assertSingleToken("contador", TokenType.IDENTIFIER, "contador");
        assertSingleToken("_x1", TokenType.IDENTIFIER, "_x1");
    }

    @Test
    void naoDeveConfundirIdentificadorComPalavraChave() {
        assertSingleToken("classe", TokenType.IDENTIFIER, "classe");
    }

    @Test
    void deveReconhecerSimbolos() {
        for (String symbol : new String[] {
                "{", "}", "(", ")", "[", "]", ".", ",", ";",
                "+", "-", "*", "/", "&", "|", "<", ">", "=", "~" }) {
            assertSingleToken(symbol, TokenType.SYMBOL, symbol);
        }
    }

    @Test
    void deveReconhecerConstantesInteiras() {
        assertSingleToken("0", TokenType.INTEGER_CONSTANT, "0");
        assertSingleToken("123", TokenType.INTEGER_CONSTANT, "123");
        assertSingleToken("32767", TokenType.INTEGER_CONSTANT, "32767");
    }

    @Test
    void deveReconhecerConstantesDeString() {
        assertSingleToken("\"Olá\"", TokenType.STRING_CONSTANT, "Olá");
        assertSingleToken("\"\"", TokenType.STRING_CONSTANT, "");
        assertSingleToken(
            "\"com espaço \"", TokenType.STRING_CONSTANT, "com espaço ");
    }

    @Test
    void naoDeveTratarComentarioDentroDeStringComoComentario() {
        assertSingleToken(
            "\"// nao e comentario\"",
            TokenType.STRING_CONSTANT,
            "// nao e comentario");
    }

    // ---------- entradas inválidas ----------

    @Test
    void deveRejeitarInteiroForaDoLimite() {
        assertThrows(RuntimeException.class, () -> tokenize("32768"));
    }

    @Test
    void deveRejeitarStringNaoTerminada() {
        assertThrows(
            RuntimeException.class,
            () -> tokenize("\"texto sem fechamento"));
    }

    @Test
    void deveRejeitarQuebraDeLinhaDentroDeString() {
        assertThrows(
            RuntimeException.class,
            () -> tokenize("\"linha1\nlinha2\""));
    }

    @Test
    void deveRejeitarCaractereInvalido() {
        assertThrows(RuntimeException.class, () -> tokenize("@"));
    }

    // ---------- espaços e comentários ----------

    @Test
    void deveIgnorarEspacosEmBranco() {
        assertTokenValues(
            "class \t Main\r\n { \n }",
            "class", "Main", "{", "}");
    }

    @Test
    void deveIgnorarComentariosDeLinha() {
        assertTokenValues(
            "class Main { // comentário de linha\n"
                + "function void main() { return; } // outro",
            "class", "Main", "{",
            "function", "void", "main", "(", ")",
            "{", "return", ";", "}");
    }

    @Test
    void deveIgnorarComentariosDeBloco() {
        assertTokenValues(
            "class /* comentário */ Main { }",
            "class", "Main", "{", "}");
    }

    @Test
    void deveIgnorarComentariosMultilinha() {
        assertTokenValues(
            "class Main {\n"
                + "/* comentário\n"
                + "   em várias linhas\n"
                + "*/\n"
                + "function void main() { return; }\n"
                + "}",
            "class", "Main", "{",
            "function", "void", "main", "(", ")",
            "{", "return", ";", "}", "}");
    }

    @Test
    void deveRejeitarComentarioDeBlocoNaoTerminado() {
        assertThrows(
            RuntimeException.class,
            () -> tokenize("class Main { /* sem fechamento"));
    }

    // ---------- arquivos oficiais ----------

    /** Todo .jack em test/fixtures, com o XML de test/expected correspondente. */
    static Stream<Arguments> officialFiles() throws IOException {
        Path fixtures = Path.of("test", "fixtures");
        Path expected = Path.of("test", "expected");

        try (Stream<Path> paths = Files.walk(fixtures)) {
            return paths
                .filter(p -> p.getFileName().toString().endsWith(".jack"))
                .sorted()
                .map(jack -> {
                    Path relative = fixtures.relativize(jack);
                    String name = jack.getFileName().toString();
                    String base = name.substring(0, name.length() - ".jack".length());

                    Path xml = expected.resolve(relative)
                        .resolveSibling(base + "T.xml");

                    return Arguments.of(relative.toString(), jack, xml);
                })
                .toList()
                .stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("officialFiles")
    void deveGerarOsMesmosTokensDoArquivoOficial(
            String name, Path jack, Path expectedXml) throws Exception {

        assertTrue(
            Files.exists(expectedXml),
            "XML de referência não encontrado: " + expectedXml);

        List<Token> actual = tokenize(Files.readString(jack));

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(
            "http://apache.org/xml/features/disallow-doctype-decl", true);

        Document document = factory.newDocumentBuilder()
            .parse(expectedXml.toFile());
        Element root = document.getDocumentElement();

        assertEquals("tokens", root.getTagName());

        List<String> expectedTypes = new ArrayList<>();
        List<String> expectedValues = new ArrayList<>();

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element element = (Element) node;
            expectedTypes.add(element.getTagName());
            expectedValues.add(unwrap(element.getTextContent()));
        }

        assertEquals(
            expectedTypes.size(), actual.size(),
            name + ": quantidade de tokens diferente");

        for (int i = 0; i < actual.size(); i++) {
            Token token = actual.get(i);

            assertEquals(
                expectedTypes.get(i), toXmlTokenType(token.getType()),
                name + ": tipo divergente no token " + (i + 1));
            assertEquals(
                expectedValues.get(i), token.getValue(),
                name + ": valor divergente no token " + (i + 1));
        }
    }

    /** Remove só o espaço de cada lado de "<tag> valor </tag>", preservando espaços do valor. */
    private static String unwrap(String text) {
        if (text.length() >= 2
                && text.startsWith(" ")
                && text.endsWith(" ")) {
            return text.substring(1, text.length() - 1);
        }
        return text.trim();
    }
}