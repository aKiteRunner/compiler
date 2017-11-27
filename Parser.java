import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Parser {
    private static final String[] KEYWORD = new String[]{
            "const", "var", "procedure", "odd", "if", "then", "begin",
            "end", "repeat", "until", "read", "write"
    };
    private static final HashSet<String> KEYWORDS = new HashSet<String>(Arrays.asList(KEYWORD));
    public ArrayList<Symbol> symbols;
    public ArrayList<String> words;
    private PushbackReader reader;

    public Parser(String filename) throws FileNotFoundException{
        this.reader = new PushbackReader(new FileReader(filename), 10);
        this.symbols = new ArrayList<>();
        this.words = new ArrayList<>();
    }

    private String readIdentifier() throws IOException{
        StringBuilder res = new StringBuilder();
        int c = reader.read();
        while (Character.isLetter(c) || Character.isDigit(c)) {
            res.append((char) c);
            c = reader.read();
        }
        reader.unread(c);
        return res.toString();
    }

    private String readNumber() throws IOException{
        StringBuilder res = new StringBuilder();
        int c = reader.read();
        while (Character.isDigit(c)) {
            res.append((char) c);
            c = reader.read();
        }
        // 当读到小数点，且后一个为数字时，其为小数
        if (c == '.') {
            if (Character.isDigit((c = reader.read()))) {
                res.append('.');
                while (Character.isDigit(c)) {
                    res.append((char) c);
                    c = reader.read();
                }
            } else {
                reader.unread(c);
                c = '.';
            }
        }
        reader.unread(c);
        return res.toString();
    }

    private String readLess() throws IOException{
        int c = reader.read();
        int next = reader.read();
        if (next == '>') {
            return "<>";
        } else if (next == '='){
            return "<=";
        } else {
            reader.unread(next);
            return "<";
        }
    }

    private String readGreater() throws IOException {
        int c = reader.read();
        int next = reader.read();
        if (next == '=') {
            return ">=";
        } else {
            reader.unread(next);
            return ">";
        }
    }

    private String readColon() throws IOException, ParseException{
        int c = reader.read();
        int next = reader.read();
        if (next == '=') {
            return ":=";
        }
        reader.unread(next);
        return ":";
    }

    private void HandleSingleChar() throws ParseException, IOException {
        int c = reader.read();
        switch (c) {
            case '(':
                symbols.add(Symbol.LeftParentheses);
                break;
            case ')':
                symbols.add(Symbol.RightParentheses);
                break;
            case '+':
                symbols.add(Symbol.Plus);
                break;
            case '-':
                symbols.add(Symbol.Minus);
                break;
            case '*':
                symbols.add(Symbol.Star);
                break;
            case '/':
                symbols.add(Symbol.Divide);
                break;
            case '=':
                symbols.add(Symbol.Equal);
                break;
            case ';':
                symbols.add(Symbol.Semi);
                break;
            case '.':
                symbols.add(Symbol.Period);
                break;
            case ',':
                symbols.add(Symbol.Comma);
                break;
            default:
                if (!Character.isWhitespace(c)) {
                    throw new ParseException(String.format("%c is not a valid symbol", c));
                }
        }
    }

    public void parse() throws IOException, ParseException{
        int c;
        String word;
        while ((c = reader.read()) != -1) {
            while (Character.isWhitespace(c)) {
                c = reader.read();
            }
            reader.unread(c);
            if (Character.isLetter(c)) {
                word = readIdentifier();
                if (KEYWORDS.contains(word)) {
                    symbols.add(Symbol.Keyword);
                } else {
                    symbols.add(Symbol.Identifier);
                }
            } else if (Character.isDigit(c)) {
                word = readNumber();
                // 有无小数点
                if (word.indexOf('.') != -1) {
                    symbols.add(Symbol.Float);
                } else {
                    symbols.add(Symbol.Integer);
                }
            } else if (c == ':') {
                word = readColon();
                if (word.length() == 2) {
                    symbols.add(Symbol.Assign);
                } else {
                    throw new ParseException(": is not a valid symbol");
                }
            } else if (c == '<') {
                word = readLess();
                if (word.length() < 2) {
                    symbols.add(Symbol.Less);
                } else if (word.charAt(1) == '>') {
                    symbols.add(Symbol.Unequal);
                } else {
                    symbols.add(Symbol.LessEqual);
                }
            } else if (c == '>') {
                word = readGreater();
                if (word.length() < 2) {
                    symbols.add(Symbol.Greater);
                } else {
                    symbols.add(Symbol.GreaterEqual);
                }
            } else {
                word = Character.toString((char) c);
                HandleSingleChar();
            }
            words.add(word);
        }
        reader.close();
    }
}

enum Symbol {
    Keyword, Identifier, Integer, Float, Semi, Comma, Plus, Minus, Star, Divide, LeftParentheses, RightParentheses,
    Assign, Less, Equal, LessEqual, Greater, GreaterEqual, Unequal, Period,
}