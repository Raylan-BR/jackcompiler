package lexer;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String input;
    private int position;
    private int line;
    private int column;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    // Consome um caractere e atualiza a posição.
    private char advanceChar() {
        char current = input.charAt(position);
        position++;

        if (current == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        return current;
    }

    public List<Token> tokenize() {

        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char current = input.charAt(position);

            // Ignora espaços, tabulações e quebras de linha.
            if (Character.isWhitespace(current)) {
                advanceChar();
                continue;
            }

            // Ignora comentários de linha: //
            if (current == '/'
                    && position + 1 < input.length()
                    && input.charAt(position + 1) == '/') {

                while (position < input.length()
                        && input.charAt(position) != '\n') {
                    advanceChar();
                }
                continue;
            }

            // Ignora comentários de bloco: /* ... */
            if (current == '/'
                    && position + 1 < input.length()
                    && input.charAt(position + 1) == '*') {

                int commentLine = line;
                int commentColumn = column;

                advanceChar(); // /
                advanceChar(); // *

                boolean closed = false;

                while (position < input.length()) {
                    if (input.charAt(position) == '*'
                            && position + 1 < input.length()
                            && input.charAt(position + 1) == '/') {

                        advanceChar(); // *
                        advanceChar(); // /
                        closed = true;
                        break;
                    }

                    advanceChar();
                }

                if (!closed) {
                    throw new RuntimeException(
                            "Comentário não fechado na linha "
                            + commentLine + ", coluna " + commentColumn
                    );
                }

                continue;
            }

            // Guarda a posição inicial de cada token.
            int tokenLine = line;
            int tokenColumn = column;

            if (current == '"') {
                tokens.add(readString(tokenLine, tokenColumn));
                continue;
            }

            if (JackSymbols.isSymbol(current)) {
                advanceChar();

                tokens.add(new Token(
                        TokenType.SYMBOL,
                        String.valueOf(current),
                        tokenLine,
                        tokenColumn
                ));
                continue;
            }

            if (Character.isDigit(current)) {
                tokens.add(readInteger(tokenLine, tokenColumn));
                continue;
            }

            if (Character.isLetter(current) || current == '_') {
                tokens.add(readIdentifierOrKeyword(
                        tokenLine, tokenColumn
                ));
                continue;
            }

            throw new RuntimeException(
                    "Caractere inválido na linha " + line
                    + ", coluna " + column + ": " + current
            );
        }

        return tokens;
    }

    private Token readString(int tokenLine, int tokenColumn) {
        advanceChar(); // Aspas iniciais

        StringBuilder value = new StringBuilder();

        while (position < input.length()) {
            char current = input.charAt(position);

            if (current == '"') {
                advanceChar(); // Aspas finais

                return new Token(
                        TokenType.STRING_CONSTANT,
                        value.toString(),
                        tokenLine,
                        tokenColumn
                );
            }

            if (current == '\n' || current == '\r') {
                throw new RuntimeException(
                        "Quebra de linha dentro de string na linha "
                        + line + ", coluna " + column
                );
            }

            value.append(advanceChar());
        }

        throw new RuntimeException(
                "String não fechada na linha " + tokenLine
                + ", coluna " + tokenColumn
        );
    }

    private Token readInteger(int tokenLine, int tokenColumn) {
        StringBuilder value = new StringBuilder();

        while (position < input.length()
                && Character.isDigit(input.charAt(position))) {
            value.append(advanceChar());
        }

        String numberText = value.toString();

        try {
            int number = Integer.parseInt(numberText);

            if (number > 32767) {
                throw new RuntimeException(
                        "Inteiro fora do intervalo na linha "
                        + tokenLine + ", coluna " + tokenColumn
                        + ": " + numberText
                );
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Inteiro fora do intervalo na linha "
                    + tokenLine + ", coluna " + tokenColumn
                    + ": " + numberText
            );
        }

        return new Token(
                TokenType.INTEGER_CONSTANT,
                numberText,
                tokenLine,
                tokenColumn
        );
    }

    private Token readIdentifierOrKeyword(
            int tokenLine, int tokenColumn) {

        StringBuilder value = new StringBuilder();

        while (position < input.length()) {
            char current = input.charAt(position);

            if (Character.isLetterOrDigit(current) || current == '_') {
                value.append(advanceChar());
            } else {
                break;
            }
        }

        String word = value.toString();

        TokenType type = JackKeywords.isKeyword(word)
                ? TokenType.KEYWORD
                : TokenType.IDENTIFIER;

        return new Token(type, word, tokenLine, tokenColumn);
    }
}