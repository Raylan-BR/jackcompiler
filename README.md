# Geração de código VM

## 1. Integrantes

- **Raylan Bruno Santana Carvalho** — Matrícula: `20260001132`
- **Kauan Guilherme Alves Pinheiro Santos** — Matrícula: `20260001160`

## 2. Linguagem de programação

O projeto foi desenvolvido em **Java**, com o objetivo de implementar um analisador léxico, sintático e o gerador de código de máquina para a linguagem Jack, utilizada no projeto de compilador Nand2Tetris.

O ponto de entrada (Compiler.java) recebe um arquivo-fonte `.jack`, gera
as instruções de máquina em um arquivo `.vm` para validação no VM Emulator.

## 3. Estrutura do projeto

O nosso projeto é estruturado em diretórios intuitivos. O `src` é formado pelo lexer com as classes que implementam a **análise Léxica**, o parser com as classes que implementam a **análise sintática**, o codegen com as classes que implementam a **geração de código VM**.

O `test` é o diretório onde ficam os programas para validação de acordo com o padrão Nand2Tetris.

## 5. Como compilar e executar

### 5.1. No Linux
1. Abra a pasta do projeto no VS Code.
2. Na raiz do projeto compile o projeto com:
```bash
javac -d target/classes src/Compiler.java src/*/*.java
```
3. Para executar a análise léxica, sintática e gerar os arquivos `.vm`:
```bash
java -cp target/classes Compiler test/Seven/Main.jack
```
(Nesse caso especificamos um arquivo jack, mas a geração de código vm pode ser feita sob um diretório com vários arquivos jack).

### 5.2. No Windows
1. Abra a pasta do projeto no VS Code.
2. Na raiz do projeto compile o projeto com:
```bash
javac -d target/classes src/Compiler.java src/lexer/*.java src/parser/*.java src/codegen/*.java
```
3. Para geração do código vm utilize o mesmo passo **3** de **5.1**

## 6 Testes com JUnit

Pré-requisito: JDK 17 ou superior instalado (`java -version` para conferir).

```bash
# Linux/Mac
./mvnw test

# Windows
.\mvnw.cmd test
```

Se aparecer erro de `JAVA_HOME`, defina a variável apontando para a pasta do seu JDK.
A primeira execução baixa o Maven e as dependências, então precisa de internet.

```bash
# Exemplo
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
```

