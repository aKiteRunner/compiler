package op_parser;

import java.util.List;

public class Grammar {
    public NotTerminalTerm in;
    public List<Term> out;

    public Grammar(NotTerminalTerm in, List<Term> out) {
        this.in = in;
        this.out = out;
    }
}
