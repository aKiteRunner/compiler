package op_parser;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperatorPrecedenceParser {
    private Pattern pattern;
    private List<Grammar> grammars;
    private HashSet<TerminalTerm> terminalTerms;
    private HashSet<NotTerminalTerm> notTerminalTerms;
    private HashMap<NotTerminalTerm, List<Grammar>> map;

    public OperatorPrecedenceParser(char[] terms) {
        pattern = Pattern.compile(".->(.*)");
        init(terms);
    }

    public OperatorPrecedenceParser(Pattern pattern, char[] terms) {
        this.pattern = pattern;
        init(terms);
    }

    private void init(char[] terms) {
        terminalTerms = new HashSet<>();
        for (char t : terms) {
            terminalTerms.add(new TerminalTerm(t));
        }
        grammars = new ArrayList<>();
        notTerminalTerms = new HashSet<>();
        map = new HashMap<>();
    }

    private Grammar parse(String string) throws ParseError {
        string = string.trim();
        Matcher m = pattern.matcher(string);
        List<Term> out = new ArrayList<>();
        if (!m.matches()) {
            throw new ParseError("请输入正确文法");
        }
        NotTerminalTerm in = new NotTerminalTerm(string.charAt(0));
        notTerminalTerms.add(in);
        string = m.group(1);
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (terminalTerms.contains(new Term(c))) {
                out.add(new TerminalTerm(c));
            } else {
                NotTerminalTerm temp = new NotTerminalTerm(c);
                out.add(temp);
                notTerminalTerms.add(temp);
            }
        }
        return new Grammar(in, out);
    }

    public void parse(Collection<? extends String> strings) throws ParseError {
        Grammar grammar;
        for (String s : strings) {
            grammar = parse(s);
            grammars.add(grammar);
            List<Grammar> list = map.get(grammar.in);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(grammar);
            map.put(grammar.in, list);
        }
    }

    public List<String> showGrammar() {
        List<String> ret = new ArrayList<>();
        for (NotTerminalTerm term : map.keySet()) {
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

    private Set<TerminalTerm> dfsFirst(NotTerminalTerm term, Set<NotTerminalTerm> visited) throws ParseError {
        visited.add(term);
        Set<TerminalTerm> ret = new HashSet<>();
        if (!notTerminalTerms.contains(term)) {
            throw new ParseError("该符号不在终结符集合里");
        }
        for (Grammar g : map.get(term)) {
            /*
             * 如果有文法为 P -> a...
             * 直接将a放入FirstVT里
             * 因为产生式的右方不会为空，故不判断长度
             */
            Term first;
            first = g.out.get(0);
            if (first instanceof TerminalTerm) {
                ret.add((TerminalTerm) first);
            } else {
                /*
                 * 如果文法为 P -> Q...
                 * 则对 Q 进行递归处理
                 */
                NotTerminalTerm temp = (NotTerminalTerm) first;
                if (!visited.contains(first)) {
                    ret.addAll(dfsFirst(temp, visited));
                }
                if (g.out.size() > 1) {
                    first = g.out.get(1);
                }
                if (first instanceof TerminalTerm) {
                    ret.add((TerminalTerm) first);
                }
            }
        }
        visited.remove(term);
        return ret;
    }

    public List<TerminalTerm> firstVT(NotTerminalTerm term) throws ParseError {
        List<TerminalTerm> ret = new ArrayList<>();
        ret.addAll(dfsFirst(term, new HashSet<>()));
        return ret;
    }

    private Set<TerminalTerm> dfsLast(NotTerminalTerm term, Set<NotTerminalTerm> visited) throws ParseError {
        visited.add(term);
        Set<TerminalTerm> ret = new HashSet<>();
        if (!notTerminalTerms.contains(term)) {
            throw new ParseError("该符号不在终结符集合里");
        }
        for (Grammar g : map.get(term)) {
            /*
             * 如果有文法为 P -> ...a
             * 直接将a放入lastVT里
             * 因为产生式的右方不会为空，故不判断长度
             */
            Term last;
            int size = g.out.size();
            last = g.out.get(size - 1);
            if (last instanceof TerminalTerm) {
                ret.add((TerminalTerm) last);
            } else {
                /*
                 * 如果文法为 P -> ...Q
                 * 则对 Q 进行递归处理
                 */
                NotTerminalTerm temp = (NotTerminalTerm) last;
                if (!visited.contains(last)) {
                    ret.addAll(dfsLast(temp, visited));
                }
                if (g.out.size() > 1) {
                    last = g.out.get(size - 2);
                }
                if (last instanceof TerminalTerm) {
                    ret.add((TerminalTerm) last);
                }
            }
        }
        visited.remove(term);
        return ret;
    }

    public List<TerminalTerm> lastVT(NotTerminalTerm term) throws ParseError {
        List<TerminalTerm> ret = new ArrayList<>();
        ret.addAll(dfsLast(term, new HashSet<>()));
        return ret;
    }
}
