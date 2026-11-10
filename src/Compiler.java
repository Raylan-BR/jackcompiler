import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import java.nio.file.StandardCopyOption;

import lexer.Lexer;
import lexer.Token;
import parser.GrammarDeclarations;
import parser.Parser;
import writer.SyntaxXmlWriter;
import writer.TokenXmlWriter;

// Classe utilitária para compilar arquivos .jack
public class Compiler {

    // Procura arquivo .jack
    private static final String JACK_EXTENSION = ".jack";

    // Verifica se o arquivo tem extensão .jack
    static boolean isJackFile(Path path) {
        return path.getFileName().toString()
            .toLowerCase()
            .endsWith(JACK_EXTENSION);
    }

    /**
     * Retorna os .jack da entrada (arquivo único ou diretório,
     * recursivamente)
     */
    static List<Path> collectJackFiles(Path input) throws IOException {
        if (Files.isRegularFile(input)) {
            if (!isJackFile(input)) {
                throw new IllegalArgumentException(
                    "O arquivo informado deve ter extensão .jack: " + input
                );
            }
            return List.of(input);
        }

        if (Files.isDirectory(input)) {
            try (Stream<Path> paths = Files.walk(input)) {
                return paths
                    .filter(Files::isRegularFile)
                    .filter(Compiler::isJackFile)
                    .sorted()
                    .toList();
            }
        }

        throw new IllegalArgumentException(
            "Caminho não encontrado: " + input
        );
    }

        /**
     * Calcula o caminho do .vm correspondente a um .jack.
     * Espera caminhos absolutos e normalizados.
     *
     * - Sem outputDir: o .vm fica ao lado do .jack.
     * - Com outputDir: subpastas relativas à entrada são preservadas.
     *   Para entrada de arquivo único, o .vm vai direto em outputDir.
     */
    static Path resolveOutputPath(
            Path input, Path jackFile, Path outputDir) {

        String name = jackFile.getFileName().toString();
        String vmName = name.substring(
            0, name.length() - JACK_EXTENSION.length()
        ) + ".vm";

        if (outputDir == null) {
            return jackFile.resolveSibling(vmName);
        }

        Path root = Files.isDirectory(input)
            ? input
            : input.getParent();

        Path relativeDir = root.relativize(jackFile).getParent();

        Path targetDir = relativeDir == null
            ? outputDir
            : outputDir.resolve(relativeDir);

        return targetDir.resolve(vmName);
    }
    
    private static String baseName(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }

    /**
     * Compila um .jack de forma independente. Em caso de erro, lança
     * exceção e não deixa nenhuma saída (nem antiga) para esse arquivo.
     */
    static void compileFile(Path jackFile, Path outputFile)
            throws IOException {

        Path outputDir = outputFile.toAbsolutePath().getParent();
        String base = baseName(outputFile);

        Path tokenXml = outputDir.resolve(base + "T.xml");
        Path syntaxXml = outputDir.resolve(base + "P.xml");

        try {
            String source = Files.readString(jackFile);
            List<Token> tokens = new Lexer(source).tokenize();

            Files.createDirectories(outputDir);

            Path tmpToken = Files.createTempFile(
                    outputDir, "jackc-", ".T.tmp");
            Path tmpSyntax = Files.createTempFile(
                    outputDir, "jackc-", ".P.tmp");

            try {
                Files.writeString(
                        tmpToken, new TokenXmlWriter().write(tokens));

                try (SyntaxXmlWriter writer =
                             new SyntaxXmlWriter(tmpSyntax)) {
                    Parser parser = new Parser(tokens, writer);
                    new GrammarDeclarations(parser, writer)
                            .compileClass();
                }

                // TODO(codegen): quanationEngindo o Compile existir,
                // gerar o .vm aqui em um terceiro arquivo temporário
                // (via VMWriter) e movê-lo para outputFile abaixo.

                Files.move(tmpToken, tokenXml,
                        StandardCopyOption.REPLACE_EXISTING);
                Files.move(tmpSyntax, syntaxXml,
                        StandardCopyOption.REPLACE_EXISTING);

            } finally {
                Files.deleteIfExists(tmpToken);
                Files.deleteIfExists(tmpSyntax);
            }

        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(outputFile);
            Files.deleteIfExists(tokenXml);
            Files.deleteIfExists(syntaxXml);
            throw e;
        }
    }
}