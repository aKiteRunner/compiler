package op_parser;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperatorPrecedenceParser {
    private Pattern pattern;
    private char[] terms;
    private List<Grammar> grammars;
    public OperatorPrecedenceParser(char[] terms) {
        pattern = Pattern.compile(".->(.*)");
        this.terms = terms;
        this.grammars = new ArrayList<>();
    }

    public OperatorPrecedenceParser(Pattern pattern, char[] terms) {
        this.pattern = pattern;
        this.terms = terms;
        this.grammars = new ArrayList<>();
    }

    public Grammar parse(String string) throws ParseError{
        string = string.trim();
        Matcher m = pattern.matcher(string);
        List<Term> out = new ArrayList<>();
        if (!m.matches()) {
            throw new ParseError("请输入正确文法");
        }
        NotTerminalTerm in = new NotTerminalTerm(string.charAt(0));
        string = m.group(1);
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (contains(c)) {
                out.add(new TerminalTerm(c));
            } else {
                out.add(new NotTerminalTerm(c));
            }
        }
        Grammar grammar = new Grammar(in, out);
        grammars.add(grammar);
        return grammar;
    }

    private boolean contains(char c) {
        int index;
        for (index  = 0; index  < terms.length; index ++) {
            if (c == terms[index]) break;
        }
        return index != terms.length;
    }

    public List<String> showGrammar() {
        List<String> ret = new ArrayList<>();
        HashMap<NotTerminalTerm, List<Grammar>> map = new HashMap<>();
        for (Grammar grammar : grammars) {
            List<Grammar> list = map.get(grammar.in);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(grammar);
            map.put(grammar.in, list);
        }
        for (NotTerminalTerm term: map.keySet()) {
            StringBuilder builder = new StringBuilder();
            List<Grammar> list = map.get(term);
            builder.append(term);
            builder.append("->");
            for (Grammar grammar : list) {
                for (Term term1 : grammar.out) {
                    builder.append(term1);
                }
                builder.append("|");
            }
            builder.delete(builder.length() - 1, builder.length());
            ret.add(builder.toString());
        }
        return ret;
    }
}
