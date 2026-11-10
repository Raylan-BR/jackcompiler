import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import lexer.Lexer;
import lexer.Token;
import parser.GrammarDeclarations;
import parser.Parser;
import codegen.SymbolTable;
import codegen.VMWriter;

// Classe utilitária para compilar arquivos .jack em arquivos .vm
public class Compiler {

    private static final String JACK_EXTENSION = ".jack";

    static boolean isJackFile(Path path) {
        return path.getFileName().toString()
            .toLowerCase()
            .endsWith(JACK_EXTENSION);
    }

    /**
     * Retorna os arquivos .jack da entrada (arquivo único ou diretório recursivamente).
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
     */
    static Path resolveOutputPath(Path input, Path jackFile, Path outputDir) {

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

    /**
     * Compila um arquivo .jack gerando o arquivo .vm final de forma atômica.
     */
    static void compileFile(Path jackFile, Path outputFile) throws IOException {

        Path outputDir = outputFile.toAbsolutePath().getParent();
        Files.createDirectories(outputDir);

        // Cria arquivo temporário .vm no diretório de destino
        Path tmpVm = Files.createTempFile(outputDir, "jackc-", ".vm.tmp");

        try {
            // 1. Análise Léxica
            String source = Files.readString(jackFile);
            List<Token> tokens = new Lexer(source).tokenize();

            // 2. Instanciação do Pipeline
            Parser parser = new Parser(tokens);
            SymbolTable symbolTable = new SymbolTable();
            VMWriter vmWriter = new VMWriter(tmpVm.toFile());

            // 3. Compilação e Geração de Código VM
            GrammarDeclarations compilationEngine =
                    new GrammarDeclarations(parser, symbolTable, vmWriter);

            compilationEngine.compileClass();

            // 4. Substituição atômica pelo arquivo .vm final
            Files.move(tmpVm, outputFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(outputFile);
            throw e;
        } finally {
            Files.deleteIfExists(tmpVm);
        }
    }

    record Options(Path input, Path outputDir) { }

    static Options parseArgs(String[] args) {
        Path input = null;
        Path outputDir = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-o") || arg.equals("--output")) {
                if (outputDir != null) {
                    throw new IllegalArgumentException(
                        "A opção -o foi informada mais de uma vez.");
                }
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException(
                        "A opção -o exige um diretório de saída.");
                }
                outputDir = Path.of(args[++i])
                    .toAbsolutePath().normalize();

            } else if (arg.startsWith("-")) {
                throw new IllegalArgumentException(
                    "Opção desconhecida: " + arg);

            } else {
                if (input != null) {
                    throw new IllegalArgumentException(
                        "Informe apenas um arquivo ou diretório de entrada.");
                }
                input = Path.of(arg).toAbsolutePath().normalize();
            }
        }

        if (input == null) {
            throw new IllegalArgumentException("Nenhuma entrada informada.");
        }

        if (!Files.exists(input)) {
            throw new IllegalArgumentException("Caminho não encontrado: " + input);
        }

        if (outputDir != null
                && Files.exists(outputDir)
                && !Files.isDirectory(outputDir)) {
            throw new IllegalArgumentException(
                "O destino de saída não é um diretório: " + outputDir);
        }

        return new Options(input, outputDir);
    }

    private static void printUsage() {
        System.err.println(
            "Uso: java Compiler <arquivo.jack | diretório> [-o <diretório-de-saída>]");
    }

    static int run(String[] args) {
        Options options;

        try {
            options = parseArgs(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Erro: " + e.getMessage());
            printUsage();
            return 2;
        }

        List<Path> files;

        try {
            files = collectJackFiles(options.input());
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Erro: " + e.getMessage());
            return 1;
        }

        if (files.isEmpty()) {
            System.err.println(
                "Erro: nenhum arquivo .jack encontrado em " + options.input());
            return 1;
        }

        int compiled = 0;
        List<Path> failed = new ArrayList<>();

        for (Path file : files) {
            Path output = resolveOutputPath(
                options.input(), file, options.outputDir());

            try {
                compileFile(file, output);
                compiled++;
                System.out.println("[OK]   " + file + " -> " + output.getFileName());
            } catch (IOException | RuntimeException e) {
                failed.add(file);
                System.err.println("[ERRO] " + file + ": " + e.getMessage());
            }
        }

        System.out.printf(
            "%nResumo: %d compilado(s), %d com erro.%n",
            compiled, failed.size());

        return failed.isEmpty() ? 0 : 1;
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }
}