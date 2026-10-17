import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class LexerTest {

    public static void main(String[] args) {
        // Testes de tokens básicos
        testKeywords();
        testIdentifiers();
        testSymbols();
        testIntegerConstants();
        testStringConstants();
        testInvalidInteger();
        testUnterminatedString();
        
        // Testes de comentários e espaços em branco
        testWhitespace();
        testLineComments();
        testBlockComments();
        testMultilineComments();
        testUnterminatedBlockComment();

        testOfficialFiles();

        System.out.println("\nTodos os testes foram executados!");
    }

    private static void testKeywords() {
        assertSingleToken("class", TokenType.KEYWORD, "class");
        assertSingleToken("function", TokenType.KEYWORD, "function");
        assertSingleToken("return", TokenType.KEYWORD, "return");

        System.out.println("PASSOU: palavras-chave");
    }

    private static void testIdentifiers() {
        assertSingleToken("Main", TokenType.IDENTIFIER, "Main");
        assertSingleToken("main", TokenType.IDENTIFIER, "main");
        assertSingleToken("contador", TokenType.IDENTIFIER, "contador");

        System.out.println("PASSOU: identificadores");
    }

    private static void testSymbols() {
        String[] symbols = {"{", "}", "(", ")", ";", "="};

        for (String symbol : symbols) {
            assertSingleToken(symbol, TokenType.SYMBOL, symbol);
        }

        System.out.println("PASSOU: símbolos");
    }

    private static void testIntegerConstants() {
        assertSingleToken("0", TokenType.INTEGER_CONSTANT, "0");
        assertSingleToken("123", TokenType.INTEGER_CONSTANT, "123");
        assertSingleToken("32767", TokenType.INTEGER_CONSTANT, "32767");

        System.out.println("PASSOU: constantes inteiras");
    }

    private static void testStringConstants() {
        assertSingleToken("\"Olá\"", TokenType.STRING_CONSTANT, "Olá");
        assertSingleToken("\"\"", TokenType.STRING_CONSTANT, "");

        System.out.println("PASSOU: constantes de string");
    }

    private static void testInvalidInteger() {
        assertInvalidInput("32768");

        System.out.println("PASSOU: rejeição de inteiro fora do limite");
    }

    private static void testUnterminatedString() {
        assertInvalidInput("\"texto sem fechamento");

        System.out.println("PASSOU: rejeição de string não terminada");
    }

    
    private static void testWhitespace() {
        String input = "class \t Main\r\n { \n }";

        assertTokenSequence(
            input,
            "class", "Main", "{", "}"
        );

        System.out.println("PASSOU: espaços em branco");
    }

    
    
    private static void testLineComments() {
        String input =
            "class Main { // comentário de linha\n" +
            "function void main() { return; } // outro comentário";

        assertTokenSequence(
            input,
            "class", "Main", "{",
            "function", "void", "main", "(",
            ")", "{", "return", ";", "}"
        );

        System.out.println("PASSOU: comentários de linha");
    }

    
    private static void testBlockComments() {
        String input =
            "class /* comentário */ Main { }";

        assertTokenSequence(
            input,
            "class", "Main", "{", "}"
        );

        System.out.println("PASSOU: comentários de bloco");
    }

    
    private static void testMultilineComments() {
        String input =
            "class Main {\n" +
            "/* comentário\n" +
            "   em várias linhas\n" +
            "*/\n" +
            "function void main() { return; }\n" +
            "}";

        assertTokenSequence(
            input,
            "class", "Main", "{",
            "function", "void", "main", "(",
            ")", "{", "return", ";", "}", "}"
        );

        System.out.println("PASSOU: comentários multilinha");
    }

    
    private static void testUnterminatedBlockComment() {
        assertInvalidInput("class Main { /* comentário sem fechamento");

        System.out.println("PASSOU: rejeição de comentário não terminado");
    }

    private static void testOfficialFiles() {
        String[][] files = {
            {"Main.jack", "MainT.xml"},
            {"Square.jack", "SquareT.xml"},
            {"SquareGame.jack", "SquareGameT.xml"}
        };

        for (String[] pair : files) {
            Path sourcePath = Path.of(
                "test", "fixtures", pair[0]
            );

            Path expectedPath = Path.of(
                "test", "expected", pair[1]
            );

            System.out.println("Diretório atual: " +
                System.getProperty("user.dir"));

            System.out.println("XML esperado: " +
                expectedPath.toAbsolutePath());

            System.out.println("Arquivo existe? " +
                Files.exists(expectedPath));

            try {
                String input = Files.readString(sourcePath);
                Lexer lexer = new Lexer(input);
                List<Token> actualTokens = lexer.tokenize();

                assertMatchesExpectedXml(
                    actualTokens,
                    expectedPath,
                    pair[0]
                );

                System.out.println(
                    "PASSOU: " + pair[0]
                    + " — " + actualTokens.size() + " tokens conferidos"
                );

            } catch (IOException e) {
                throw new RuntimeException(
                    "Erro ao ler os arquivos de teste: " + pair[0],
                    e
                );
            }
        }
    }

    
    private static void assertMatchesExpectedXml(
            List<Token> actualTokens,
            Path expectedPath,
            String sourceName) throws IOException {

        try {
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

            factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
            );

            Document document = factory
                .newDocumentBuilder()
                .parse(expectedPath.toFile());

            Element root = document.getDocumentElement();

            if (!root.getTagName().equals("tokens")) {
                throw new AssertionError(
                    "XML de referência inválido: " + expectedPath
                );
            }

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
                expectedValues.add(element.getTextContent().trim());
            }

            if (actualTokens.size() != expectedTypes.size()) {
                throw new AssertionError(
                    sourceName
                    + ": quantidade de tokens diferente. Esperado: "
                    + expectedTypes.size()
                    + ", recebido: " + actualTokens.size()
                );
            }

            for (int i = 0; i < actualTokens.size(); i++) {
                Token actual = actualTokens.get(i);

                String expectedType = expectedTypes.get(i);
                String expectedValue = expectedValues.get(i);

                String actualType = toXmlTokenType(actual.getType());

                if (!actualType.equals(expectedType)
                        || !actual.getValue().equals(expectedValue)) {

                    throw new AssertionError(
                        sourceName
                        + ": divergência no token " + (i + 1)
                        + "\nTipo esperado: " + expectedType
                        + "\nTipo recebido: " + actualType
                        + "\nValor esperado: [" + expectedValue + "]"
                        + "\nValor recebido: [" + actual.getValue() + "]"
                    );
                }
            }

        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                "Não foi possível comparar com o XML: " + expectedPath,
                e
            );
        }
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
                    "Tipo de token desconhecido: " + type
                );
        }
    }

    private static void assertTokenSequence(
            String input,
            String... expectedValues) {

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();

        if (tokens.size() != expectedValues.length) {
            throw new AssertionError(
                "Quantidade de tokens incorreta. Esperado: "
                + expectedValues.length
                + ", recebido: " + tokens.size()
                + "\nEntrada: " + input
                + "\nTokens: " + tokens
            );
        }

        for (int i = 0; i < expectedValues.length; i++) {
            String actualValue = tokens.get(i).getValue();

            if (!actualValue.equals(expectedValues[i])) {
                throw new AssertionError(
                    "Token na posição " + i
                    + ": esperado [" + expectedValues[i]
                    + "], recebido [" + actualValue + "]"
                );
            }
        }
    }

    private static void assertSingleToken(
            String input,
            TokenType expectedType,
            String expectedValue) {

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();

        if (tokens.size() != 1) {
            throw new AssertionError(
                "Entrada: " + input
                + " | Esperado 1 token, recebido: " + tokens.size()
            );
        }

        Token token = tokens.get(0);

        if (token.getType() != expectedType) {
            throw new AssertionError(
                "Entrada: " + input
                + " | Tipo esperado: " + expectedType
                + " | Tipo recebido: " + token.getType()
            );
        }

        if (!token.getValue().equals(expectedValue)) {
            throw new AssertionError(
                "Entrada: " + input
                + " | Valor esperado: [" + expectedValue
                + "] | Valor recebido: [" + token.getValue() + "]"
            );
        }
    }

    private static void assertInvalidInput(String input) {
        try {
            Lexer lexer = new Lexer(input);
            lexer.tokenize();

            throw new AssertionError(
                "Era esperada uma exceção para a entrada: " + input
            );
        } catch (AssertionError error) {
            throw error;
        } catch (RuntimeException expected) {
            // A entrada inválida foi rejeitada como esperado.
        }
    }
}