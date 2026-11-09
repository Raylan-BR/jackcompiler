import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public enum Kind {
        STATIC, FIELD, ARG, VAR, NONE
    }

    private static class Symbol {
        private final String type;
        private final Kind kind;
        private final int index;

        public Symbol(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        public String getType() { return type; }
        public Kind getKind() { return kind; }
        public int getIndex() { return index; }
    }

    private final Map<String, Symbol> classSymbols;
    private final Map<String, Symbol> subroutineSymbols;
    private final Map<Kind, Integer> indices;

    public SymbolTable() {
        classSymbols = new HashMap<>();
        subroutineSymbols = new HashMap<>();
        indices = new HashMap<>();

        indices.put(Kind.STATIC, 0);
        indices.put(Kind.FIELD, 0);
        indices.put(Kind.ARG, 0);
        indices.put(Kind.VAR, 0);
    }

    /**
     * Reinicia a tabela de símbolos do escopo da subrotina (método/função/construtor).
     */
    public void startSubroutine() {
        subroutineSymbols.clear();
        indices.put(Kind.ARG, 0);
        indices.put(Kind.VAR, 0);
    }

    /**
     * Define um novo identificador e atribui a ele o próximo índice disponível da sua categoria.
     */
    public void define(String name, String type, Kind kind) {
        int index = indices.get(kind);
        Symbol symbol = new Symbol(type, kind, index);
        indices.put(kind, index + 1);

        if (kind == Kind.STATIC || kind == Kind.FIELD) {
            classSymbols.put(name, symbol);
        } else if (kind == Kind.ARG || kind == Kind.VAR) {
            subroutineSymbols.put(name, symbol);
        }
    }

    /**
     * Retorna o número de variáveis salvas de uma determinada categoria no escopo atual.
     */
    public int varCount(Kind kind) {
        return indices.getOrDefault(kind, 0);
    }

    /**
     * Busca a categoria do identificador no escopo local primeiro, depois no escopo de classe.
     */
    public Kind kindOf(String name) {
        if (subroutineSymbols.containsKey(name)) {
            return subroutineSymbols.get(name).getKind();
        }
        if (classSymbols.containsKey(name)) {
            return classSymbols.get(name).getKind();
        }
        return Kind.NONE;
    }

    /**
     * Retorna o tipo da variável buscada.
     */
    public String typeOf(String name) {
        if (subroutineSymbols.containsKey(name)) {
            return subroutineSymbols.get(name).getType();
        }
        if (classSymbols.containsKey(name)) {
            return classSymbols.get(name).getType();
        }
        return "";
    }

    /**
     * Retorna o índice numérico atribuído à variável.
     */
    public int indexOf(String name) {
        if (subroutineSymbols.containsKey(name)) {
            return subroutineSymbols.get(name).getIndex();
        }
        if (classSymbols.containsKey(name)) {
            return classSymbols.get(name).getIndex();
        }
        return -1;
    }
}