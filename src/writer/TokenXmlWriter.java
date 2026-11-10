package writer;
import java.util.List;

import lexer.Token;
import lexer.TokenType;

public class TokenXmlWriter {

    public String write(List<Token> tokens) {
        // Gerar a representação XML dos tokens
        StringBuilder xml = new StringBuilder();

        // Adicionar a declaração XML
        xml.append("<tokens>\n");

        // Gerar a representação XML para cada token
        for (Token token : tokens) {
            String tag = getTagName(token.getType());
            String value = escapeXml(token.getValue());

            xml.append("<")
            .append(tag)
            .append("> ")
            .append(value)
            .append(" </")
            .append(tag)
            .append(">\n");
        }

        xml.append("</tokens>\n");

        return xml.toString();
    }

    // Método auxiliar para obter o nome da tag XML com base no tipo de token
    private String getTagName(TokenType type) {
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

    // Método auxiliar para os escapes de caracteres especiais em XML
    private String escapeXml(String value) {
        StringBuilder escaped = new StringBuilder();

        for (char c : value.toCharArray()) {
            switch (c) {
                case '&':
                    escaped.append("&amp;");
                    break;

                case '<':
                    escaped.append("&lt;");
                    break;

                case '>':
                    escaped.append("&gt;");
                    break;

                case '"':
                    escaped.append("&quot;");
                    break;

                default:
                    escaped.append(c);
            }
        }

        return escaped.toString();
    }

}
