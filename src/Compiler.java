import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    // Classe para armazenar as opções de entrada e saída do compilador
    record Options(Path input, Path outputDir) { }

    // Analisa os argumentos da linha de comando e retorna as opções correspondentes.
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
            throw new IllegalArgumentException(
                "Nenhuma entrada informada.");
        }

        if (!Files.exists(input)) {
            throw new IllegalArgumentException(
                "Caminho não encontrado: " + input);
        }

        if (outputDir != null
                && Files.exists(outputDir)
                && !Files.isDirectory(outputDir)) {
            throw new IllegalArgumentException(
                "O destino de saída não é um diretório: " + outputDir);
        }

        return new Options(input, outputDir);
    }

    // Imprime a mensagem de uso do compilador.
    private static void printUsage() {
        System.err.println(
            "Uso: java Compiler <arquivo.jack | diretório> [-o <diretório-de-saída>]");
    }

    /** Executa o compilador e retorna o código de saída (0, 1 ou 2). */
    // 0: sucesso, 1: erro de compilação, 2: erro de uso
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
                "Erro: nenhum arquivo .jack encontrado em "
                    + options.input());
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
                System.out.println("[OK]   " + file);
            } catch (IOException | RuntimeException e) {
                failed.add(file);
                System.err.println(
                    "[ERRO] " + file + ": " + e.getMessage());
            }
        }

        System.out.printf(
            "%nResumo: %d compilado(s), %d com erro.%n",
            compiled, failed.size());

        return failed.isEmpty() ? 0 : 1;
    }

    // Ponto de entrada do compilador. Chama run() e encerra o programa com o código de saída apropriado.
    public static void main(String[] args) {
        System.exit(run(args));
    }
}