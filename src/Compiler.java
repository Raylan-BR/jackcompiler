import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

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
}