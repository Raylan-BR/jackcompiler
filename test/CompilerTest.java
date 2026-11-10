import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CompilerTest {

    @TempDir
    Path tmp;

    private Path create(String relative) throws IOException {
        Path file = tmp.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, "");
        return file;
    }

    // ---------- collectJackFiles ----------

    @Test
    void deveAceitarArquivoJackIndividual() throws IOException {
        Path file = create("Main.jack");

        assertEquals(List.of(file), Compiler.collectJackFiles(file));
    }

    @Test
    void deveColetarRecursivamenteEmOrdemOrdenada() throws IOException {
        create("Main.jack");
        create("sub/Square.jack");
        create("sub/deep/Game.jack");
        create("notas.txt");

        List<Path> files = Compiler.collectJackFiles(tmp);

        assertEquals(3, files.size());
        assertEquals(files.stream().sorted().toList(), files);
        assertTrue(files.stream().allMatch(Compiler::isJackFile));
    }

    @Test
    void deveRetornarListaVaziaParaDiretorioSemJack() throws IOException {
        create("notas.txt");

        assertTrue(Compiler.collectJackFiles(tmp).isEmpty());
    }

    @Test
    void deveRejeitarArquivoSemExtensaoJack() throws IOException {
        Path file = create("notas.txt");

        assertThrows(
            IllegalArgumentException.class,
            () -> Compiler.collectJackFiles(file)
        );
    }

    @Test
    void deveRejeitarCaminhoInexistente() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Compiler.collectJackFiles(tmp.resolve("nao-existe"))
        );
    }
}