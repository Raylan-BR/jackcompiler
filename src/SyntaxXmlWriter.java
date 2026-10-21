import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public class SyntaxXmlWriter implements AutoCloseable {

    private final BufferedWriter writer;
    private final Deque<String> tagStack = new ArrayDeque<>();
    private int indentation = 0;

    public SyntaxXmlWriter(Path outputPath) throws IOException {
        this.writer = Files.newBufferedWriter(outputPath);
    }

    public void openTag(String tag) {
        try {
            writeIndentation();
            writer.write("<" + tag + ">");
            writer.newLine();

            tagStack.push(tag);
            indentation++;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void closeTag(String tag) {
        if (tagStack.isEmpty()) {
            throw new IllegalStateException(
                "Não existe tag aberta para fechar: " + tag
            );
        }

        if (!tagStack.peek().equals(tag)) {
            throw new IllegalStateException(
                "Tag esperada para fechamento: </"
                    + tagStack.peek() + ">, mas recebida: </"
                    + tag + ">"
            );
        }

        try {
            indentation--;
            writeIndentation();
            writer.write("</" + tag + ">");
            writer.newLine();

            tagStack.pop();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeToken(String type, String value) {
        try {
            writeIndentation();
            writer.write(
                "<" + type + "> "
                    + escapeXml(value)
                    + " </" + type + ">"
            );
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeIndentation() throws IOException {
        writer.write("  ".repeat(indentation));
    }

    private String escapeXml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    @Override
    public void close() throws IOException {
        if (!tagStack.isEmpty()) {
            throw new IllegalStateException(
                "Existem tags XML ainda abertas: " + tagStack
            );
        }

        writer.close();
    }
}