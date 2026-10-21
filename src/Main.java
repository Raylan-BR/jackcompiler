import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Uso: java Main <arquivo.jack>");
            System.exit(1);
        }

        Path filePath = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        if (!filePath.getFileName().toString()
                .toLowerCase()
                .endsWith(".jack")) {
            System.err.println(
                "Erro: o arquivo informado deve ter extensão .jack."
            );
            System.exit(1);
        }

        if (!Files.isRegularFile(filePath)) {
            System.err.println(
                "Erro: arquivo não encontrado ou inválido: " + filePath
            );
            System.exit(1);
        }

        try {
            // 1. Lê o código-fonte Jack.
            String jackCode = Files.readString(filePath);

            // 2. Executa a análise léxica.
            Lexer lexer = new Lexer(jackCode);
            List<Token> tokens = lexer.tokenize();

            // 3. Obtém o nome-base do arquivo.
            String inputName = filePath.getFileName().toString();

            String baseName = inputName.substring(
                0,
                inputName.length() - ".jack".length()
            );

            // 4. Define a pasta de saída.
            Path parent = filePath.getParent();
            Path projectFolder = parent.getFileName();

            Path outputDir = Path.of(
                "test",
                "actual",
                projectFolder.toString()
            );

            Files.createDirectories(outputDir);

            // 5. Gera o XML léxico: NomeT.xml.
            TokenXmlWriter tokenXmlWriter = new TokenXmlWriter();
            String tokenXml = tokenXmlWriter.write(tokens);

            Path tokenOutputPath = outputDir.resolve(
                baseName + "T.xml"
            );

            Files.writeString(tokenOutputPath, tokenXml);

            System.out.println(
                "XML léxico gerado com sucesso: " + tokenOutputPath
            );

            // 6. Gera o XML sintático: NomeP.xml.
            Path syntaxOutputPath = outputDir.resolve(
                baseName + "P.xml"
            );

            try (SyntaxXmlWriter syntaxXmlWriter =
                    new SyntaxXmlWriter(syntaxOutputPath)) {

                Parser parser = new Parser(tokens, syntaxXmlWriter);

                GrammarDeclarations grammar =
                    new GrammarDeclarations(parser, syntaxXmlWriter);

                grammar.compileClass();
            }

            System.out.println(
                "XML sintático gerado com sucesso: " + syntaxOutputPath
            );

        } catch (IOException e) {
            System.err.println(
                "Erro ao ler ou salvar arquivo: " + e.getMessage()
            );
            System.exit(1);

        } catch (RuntimeException e) {
            System.err.println(
                "Erro durante a análise léxica/sintática: "
                    + e.getMessage()
            );
            System.exit(1);
        }
    }
}