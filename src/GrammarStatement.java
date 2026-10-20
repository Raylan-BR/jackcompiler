import java.util.List;

// Reponsável por compilar uma instrução (statement) da linguagem
public class GrammarStatement {
    
    private final Parser parser;

    // Construtor que recebe uma instância de Parser como parâmetro
    public GrammarStatement(Parser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("Parser não pode ser nulo");
        }
        this.parser = parser;
    }
    
    // Construtor alternativo que recebe uma lista de tokens e cria um Parser
    public GrammarStatement(List<Token> tokens) {
        this(new Parser(tokens));
    }

    // Retorna a instância do Parser 
    public Parser getParser() {
        return parser;
    }

    /**
     * Identifica o comando atual sem consumir o token.
     *
     * Retorna let, if, while, do ou return.
     * Retorna null se o token não iniciar um comando.
     */
    public String identifyNextCommand() {
        if (parser.isAtEnd()) {
            return null;
        }

        String value = parser.peek().getValue();
        switch (value) {
            case "let":
            case "if":
            case "while":
            case "do":
            case "return":
                return value;
            default:
                return null;
        }
    }

    public String getNextCommand() {
        return identifyNextCommand();
    }
}


