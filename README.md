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

Para validar os arquivos gerados, utilizamos o site a seguir:
[VM EMULATOR WEB](https://nand2tetris.github.io/web-ide/vm).

## 6 Utilização da IA no desenvolvimento

Utilizamos os chats web de IA: ChatGPT e Gemini nas etapas de análise léxica, sintática e geração de código VM. Ao longo de cada etapa, criamos sprints de desenvolvimento na plataforma jira dividindo as tasks entre a dupla.

Na primeira etapa utilizamos a IA como apoio durante o desenvolvimento do analisador léxico. Com essa ajuda, compreendemos a estrutura do lexer, implementamos o reconhecimento dos diferentes tipos de tokens, o tratamento de espaços em branco e comentários. Também utilizamos a IA para esclarecer dúvidas sobre o código, corrigir erros identificados durante os testes e validar o funcionamento do analisador com os arquivos oficiais `Main.jack`, `Square.jack` e `SquareGame.jack` do nand2tetris.

Na segunda etapa compreendemos e implementamos métodos auxiliares para navegação e validação dos tokens. Também recebemos orientações sobre a integração entre o Lexer e o Parser e sobre a criação de testes utilizando arquivos .jack para verificar o funcionamento dos métodos.

Na terceira etapa ela nos auxiliou na definição da estrutura de classes e métodos (VMWriter, SymbolTable e CompilationEngine), identificação de testes unitários e de integração. Também nos ajudou a compreender os requisitos de geração de código VM e compilação por diretórios. A implementação, integração, revisão e validação do código foram realizadas sob nossa responsabilidade, com participação ativa dos dois integrantes.

## 7 Testes com JUnit

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

