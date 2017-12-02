package op_parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OperatorPrecedenceParser {
    private Pattern pattern;
    private List<Grammar> grammars;
    private HashSet<TerminalTerm> terminalTerms;
    private HashSet<NotTerminalTerm> notTerminalTerms;
    private HashMap<NotTerminalTerm, List<Grammar>> map;

    public OperatorPrecedenceParser() {
        pattern = Pattern.compile(".->(.*)");
        init();
    }

    public OperatorPrecedenceParser(Pattern pattern) {
        this.pattern = pattern;
        init();
    }

    private void init() {
        terminalTerms = new HashSet<>();
        grammars = new ArrayList<>();
        notTerminalTerms = new HashSet<>();
        map = new HashMap<>();
    }

    private Grammar parse(String string) {
        Matcher m = pattern.matcher(string);
        m.matches();
        List<Term> out = new ArrayList<>();
        NotTerminalTerm in = new NotTerminalTerm(string.charAt(0));
        string = m.group(1);
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (notTerminalTerms.contains(new Term(c))) {
                out.add(new NotTerminalTerm(c));
            } else {
                TerminalTerm temp = new TerminalTerm(c);
                out.add(temp);
                terminalTerms.add(temp);
            }
        }
        return new Grammar(in, out);
    }

    public void parse(Collection<? extends String> strings) throws ParseError {
        Grammar grammar;
        for (String s : strings) {
            Matcher m = pattern.matcher(s);
            if (!m.matches()) {
                throw new ParseError("请输入正确文法");
            }
            notTerminalTerms.add(new NotTerminalTerm(s.charAt(0)));
        }
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

    public Relation[][] table() throws ParseError {
        int size = terminalTerms.size();
        Relation[][] relations = new Relation[size][size];
        for (Relation[] arr : relations) {
            Arrays.fill(arr, Relation.None);
        }
        Map<Term, Integer> m = tableIndex();
        Term cur, next;
        for (Grammar g : grammars) {
            for (int i = 0; i < g.out.size() - 1; i++) {
                cur = g.out.get(i);
                next = g.out.get(i + 1);
                if (terminalTerms.contains(cur) && terminalTerms.contains(next)) {
                    relations[m.get(cur)][m.get(next)] = Relation.Equal;
                } else if (terminalTerms.contains(cur) && notTerminalTerms.contains(next)) {
                    int index = m.get(cur);
                    for (Term t : firstVT((NotTerminalTerm) next)) {
                        relations[index][m.get(t)] = Relation.Less;
                    }
                } else if (notTerminalTerms.contains(cur) && terminalTerms.contains(next)) {
                    int index = m.get(next);
                    for (Term t : lastVT((NotTerminalTerm) cur)) {
                        relations[m.get(t)][index] = Relation.Greater;
                    }
                }
                if (i < g.out.size() - 2 && terminalTerms.contains(cur) && terminalTerms.contains(g.out.get(i + 2))) {
                    relations[m.get(cur)][m.get(g.out.get(i + 2))] = Relation.Equal;
                }
            }
        }
        return relations;
    }

    private Map<Term, Integer> tableIndex() {
        Map<Term, Integer> ret = new HashMap<>();
        int index = 0;
        for (TerminalTerm t : terminalTerms) {
            ret.put(t, index++);
        }
        return ret;
    }

    private Term reduce(List<Term> src) {
        int size = src.size();
        for (Grammar g : grammars) {
            if (compare(g.out, src)) return g.in;
        }
        return null;
    }

    private boolean compare(List<Term> list1, List<Term> list2) {
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            if (notTerminalTerms.contains(list1.get(i)) && notTerminalTerms.contains(list2.get(i))) {
                continue;
            }
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public List<String> makeReduction(String action) throws ParseError{
        return makeReduction(action, table(), tableIndex());
    }

    public List<String> makeReduction(String action, Relation[][] table, Map<Term, Integer> indexMap) throws ParseError {
        ArrayList<Term> stack = new ArrayList<>();
        stack.add(new Term('#'));
        action += '#';
        int step = 1;
        List<Term> src = new ArrayList<>();
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < action.length(); i++) {
            Term top = stack.get(stack.size() - 1);
            for (int j = stack.size() - 1; j >= 0; j--) {
                if (terminalTerms.contains(stack.get(j))) {
                    top = stack.get(j);
                    break;
                }
            }
            Relation relation = table[indexMap.get(top)][indexMap.get(new Term(action.charAt(i)))];
            switch (relation) {
                case Less:
                case Equal:
                    ret.add(String.format("%-14s|%-15s%-15s%-15s%-15s%s", step, printStack(stack), relation, action.charAt(i), action.substring(i + 1), "移进"));
                    stack.add(new Term(action.charAt(i)));
                    break;
                case Greater:
                    ret.add(String.format("%-14s|%-15s%-15s%-15s%-15s%s", step, printStack(stack), relation, action.charAt(i), action.substring(i + 1), "规约"));
                    int x = stack.size() - 2;
                    while (true) {
                        if (stack.size() == 0) {
                            break;
                        }
                        if (terminalTerms.contains(stack.get(x)) && table[indexMap.get(stack.get(x))][indexMap.get(top)] == Relation.Less) {
                            break;
                        }
                        x--;
                    }
                    src.clear();
                    for (int j = stack.size() - 1; j > x; j--) {
                        src.add(stack.get(j));
                        stack.remove(j);
                    }
                    Term left = reduce(src);
                    stack.add(left);
                    i--;
                    break;
                default:
                    throw new ParseError("规约失败");
            }
            step++;
        }
        return ret;
    }

    private String printStack(List<Term> stack) {
        return stack.stream().map(Term::toString).collect(Collectors.joining());
    }
}

enum Relation {
    None, Greater, Equal, Less;

    @Override
    public String toString() {
        switch (this.name()) {
            case "Greater":
                return ">";
            case "Equal":
                return "=";
            case "Less":
                return "<";
            default:
                return "";
        }
    }
}
