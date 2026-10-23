# Analisador Sintático Jack — Nand2Tetris

## 1. Integrantes

- **Raylan Bruno Santana Carvalho** — Matrícula: `20260001132`
- **Kauan Guilherme Alves Pinheiro Santos** — Matrícula: `20260001160`

## 2. Linguagem de programação

O projeto foi desenvolvido em **Java**, com o objetivo de implementar um analisador léxico e sintático para a linguagem Jack, utilizada no projeto Nand2Tetris.

O ponto de entrada (Main.jack) recebe um arquivo-fonte `.jack`, identifica suas unidades léxicas gera uma sequência de tokens e passa para análise sintática.

## 3. Estrutura do projeto

O nosso projeto é estruturado em diretórios intuitivos. O `src` é formado basicamente pelo ponto de entrada Main.java, pelo analisador Léxico Lexer.java, analisador Sintático Parser.java distribuído em classes que formam a gramática da linguagem.

O `test` é o diretório onde ficam os arquivos oficiais para execução dos testes. O subdiretório `fixtures` possui os arquivos gabarito no formato jack organizados nos níveis de testes escalados pelo projeto: ArrayTest, ExpressionLessSquare e Square. O `expected` possui a mesma organização, mas é constituído dos arquivos gabarito no formato xml que nosso compilador deve gerar. 

Na geração das saídas, os arquivos serão salvos em um diretório chamado `actual` para futura comparação com diff.

## 5. Como compilar e executar

### 5.1. No Linux
1. Abra a pasta do projeto no VS Code.
2. Na raiz do projeto compile o projeto com:
```bash
javac -d target/classes $(find src -name "*.java")
```
3. Para executar a análise léxica e sintática sob um arquivo oficial:
```bash
java -cp target/classes Main test/fixtures/ArrayTest/Main.jack
```
(Nesse caso usamos o teste Main do diretório ArrayTest, mas você pode usar os outros arquivos para confirmar a geração do xml).

### 5.2. No Windows
1. Abra a pasta do projeto no VS Code.
2. Na raiz do projeto compile o projeto com:
```bash
javac -d target/classes (Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)
```
3. Para executar a análise léxica e sintática utilize o mesmo passo **3** de **5.1**

## 6. Estratégia de testes

A estratégia de testes adotada consiste na **geração de uma saída XML pelo analisador léxico/sintático e na comparação dessa saída com os arquivos oficiais de referência do Nand2Tetris**, utilizando a ferramenta `diff`.

Essa abordagem permite verificar se os tokens identificados pelo nosso programa correspondem aos tokens esperados para o mesmo arquivo-fonte.

### 6.1. Comparação com `diff`

Após gerar o XML, a comparação pode ser realizada pelo terminal. Na etapa **3** de **5.1** nosso compilador gera o MainT.xml e o MainP.xml no diretório actual para comparação.

Para o xml da análise **léxica**:

Linux:
```bash
diff -u -w test/actual/ArrayTest/MainT.xml test/expected/ArrayTest/MainT.xml
```
windows:
```bash
Compare-Object `
    (Get-Content -Raw test/actual/ArrayTest/MainT.xml) `
    (Get-Content -Raw test/expected/ArrayTest/MainT.xml)
```
Para o xml da análise **sintática**:

Linux:
```bash
diff -u -w test/actual/ArrayTest/MainP.xml test/expected/ArrayTest/Main.xml
```
windows:
```bash
Compare-Object `
    (Get-Content -Raw test/actual/ArrayTest/MainP.xml) `
    (Get-Content -Raw test/expected/ArrayTest/Main.xml)
```


