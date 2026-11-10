import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

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

        // ---------- resolveOutputPath ----------

    @Test
    void deveGerarVmAoLadoDoJackSemDiretorioDeSaida() throws IOException {
        Path file = create("Main.jack");

        assertEquals(
            tmp.resolve("Main.vm"),
            Compiler.resolveOutputPath(file, file, null)
        );
    }

    @Test
    void devePreservarSubpastasNoDiretorioDeSaida() throws IOException {
        Path file = create("sub/deep/Game.jack");
        Path out = tmp.resolve("build");

        assertEquals(
            out.resolve("sub").resolve("deep").resolve("Game.vm"),
            Compiler.resolveOutputPath(tmp, file, out)
        );
    }

    @Test
    void deveColocarArquivoUnicoDiretamenteNoDiretorioDeSaida()
            throws IOException {
        Path file = create("proj/Main.jack");
        Path out = tmp.resolve("build");

        assertEquals(
            out.resolve("Main.vm"),
            Compiler.resolveOutputPath(file, file, out)
        );
    }

    // ---------- compileFile ----------

    private long countFiles(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return 0;
        }
        try (Stream<Path> s = Files.list(dir)) {
            return s.count();
        }
    }

    @Test
    void deveGerarSaidasParaArquivoValido() throws IOException {
        Path jack = tmp.resolve("Main.jack");
        Files.writeString(jack,
                "class Main { function void main() { return; } }");
        Path out = tmp.resolve("build").resolve("Main.vm");

        Compiler.compileFile(jack, out);

        assertTrue(Files.exists(out.resolveSibling("MainT.xml")));
        assertTrue(Files.exists(out.resolveSibling("MainP.xml")));
        assertEquals(2, countFiles(out.getParent())); // sem temporários
    }

    @Test
    void naoDeveDeixarSaidaParaArquivoComErro() throws IOException {
        Path jack = tmp.resolve("Main.jack");
        Files.writeString(jack,
                "class Main { function void main() { return } }");
        Path out = tmp.resolve("build").resolve("Main.vm");

        assertThrows(
                RuntimeException.class,
                () -> Compiler.compileFile(jack, out)
        );

        assertEquals(0, countFiles(out.getParent()));
    }

    @Test
    void deveRemoverSaidaAntigaQuandoNovaCompilacaoFalha()
            throws IOException {
        Path jack = tmp.resolve("Main.jack");
        Path out = tmp.resolve("build").resolve("Main.vm");

        Files.writeString(jack,
                "class Main { function void main() { return; } }");
        Compiler.compileFile(jack, out);

        Files.writeString(jack, "class Main {");
        assertThrows(
                RuntimeException.class,
                () -> Compiler.compileFile(jack, out)
        );

        assertEquals(0, countFiles(out.getParent()));
    }
}