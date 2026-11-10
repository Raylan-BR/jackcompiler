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
}