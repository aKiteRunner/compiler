package op_parser;

public class Term {
    public char name;
    public Term() {
        name = '\0';
    }

    public Term(char name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Term && name == ((Term) obj).name;
    }

    @Override
    public int hashCode() {
        return new Character(name).hashCode();
    }

    @Override
    public String toString() {
        return Character.toString(name);
    }
}

class TerminalTerm extends Term {
    public TerminalTerm() {
        super();
    }

    public TerminalTerm(char name) {
        super(name);
    }
}

class NotTerminalTerm extends Term {
    public NotTerminalTerm() {
        super();
    }

    public NotTerminalTerm(char name) {
        super(name);
    }
}