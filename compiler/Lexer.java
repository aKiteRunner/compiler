package compiler;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class Lexer {
    private PushbackReader reader;
    private ArrayList<Token> table;
    private ArrayList<String> errors;
    private int line;
    private Iterator<Token> iterator;

    public Lexer(String filename) throws FileNotFoundException {
        this.reader = new PushbackReader(new FileReader(filename), 10);
        init();
    }

    public Lexer(BufferedReader reader) {
        this.reader = new PushbackReader(reader, 10);
        init();
    }

    private void init() {
        this.table = new ArrayList<>();
        this.errors = new ArrayList<>();
        line = 1;
    }

    private String readIdentifier() throws IOException {
        StringBuilder res = new StringBuilder();
        int c = reader.read();
        while (Character.isLetter(c) || Character.isDigit(c)) {
            res.append((char) c);
            c = reader.read();
        }
        reader.unread(c);
        return res.toString();
    }

    private String readNumber() throws IOException {
        StringBuilder res = new StringBuilder();
        int c = reader.read();
        if (c == '0') {
            // 如果以0开头, 直接返回
            return "0";
        }
        while (Character.isDigit(c)) {
            res.append((char) c);
            c = reader.read();
        }
        reader.unread(c);
        String s = res.toString();
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException error) {
            errors.add(String.format("Line %d: %s 这个数太大", line, s));
        }
        return s;
    }

    private String readLess() throws IOException {
        int c = reader.read();
        int next = reader.read();
        if (next == '>') {
            return "<>";
        } else if (next == '=') {
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

    private String readColon() throws IOException{
        int c = reader.read();
        int next = reader.read();
        if (next == '=') {
            return ":=";
        }
        reader.unread(next);
        return ":";
    }

    private Symbol HandleSingleChar() throws IOException {
        int c = reader.read();
        Symbol symbol = Symbol.None;
        switch (c) {
            case '(':
                symbol = Symbol.LeftParentheses;
                break;
            case ')':
                symbol = Symbol.RightParentheses;
                break;
            case '+':
                symbol = Symbol.Plus;
                break;
            case '-':
                symbol = Symbol.Minus;
                break;
            case '*':
                symbol = Symbol.Star;
                break;
            case '/':
                symbol = Symbol.Slash;
                break;
            case '=':
                symbol = Symbol.Equal;
                break;
            case ';':
                symbol = Symbol.SemiColon;
                break;
            case '.':
                symbol = Symbol.Period;
                break;
            case ',':
                symbol = Symbol.Comma;
                break;
            default:
                if (c != -1 && c != 65535)
                    errors.add(String.format("Line %d: %c不是合法的字符.", line, c));
        }
        return symbol;
    }

    public void lex() throws IOException, LexException {
        int c;
        String word;
        Symbol symbol = Symbol.None;
        while ((c = reader.read()) != -1) {
            while (Character.isWhitespace(c)) {
                if (c == '\n') {
                    line++;
                }
                c = reader.read();
            }
            // 当读到末尾时，跳出循环
            if (c == -1) break;
            reader.unread(c);
            if (Character.isLetter(c)) {
                word = readIdentifier();
                // 如果不在保留字表里，则说明是标识符
                symbol = Token.getKeywordSymbol(word);
                if (symbol == Symbol.None) {
                    symbol = Symbol.Identifier;
                }
            } else if (Character.isDigit(c)) {
                word = readNumber();
                // 有无小数点
                if (word.indexOf('.') != -1) {
                    symbol = Symbol.Float;
                } else {
                    symbol = Symbol.Integer;
                }
            } else if (c == ':') {
                word = readColon();
                if (word.length() == 2) {
                    symbol = Symbol.Assign;
                } else {
                    errors.add(String.format("Line %d: %c不是合法的字符.", line, c)) ;
                }
            } else if (c == '<') {
                word = readLess();
                if (word.length() < 2) {
                    symbol = Symbol.Less;
                } else if (word.charAt(1) == '>') {
                    symbol = Symbol.Unequal;
                } else {
                    symbol = Symbol.LessEqual;
                }
            } else if (c == '>') {
                word = readGreater();
                if (word.length() < 2) {
                    symbol = Symbol.Greater;
                } else {
                    symbol = Symbol.GreaterEqual;
                }
            } else {
                word = Character.toString((char) c);
                symbol = HandleSingleChar();
            }
            table.add(new Token(word, symbol, line));
        }
        // 词法分析结束时才将错误抛出
        iterator = table.iterator();
        if (errors.size() != 0) {
            iterator = null;
            throw new LexException(errors);
        }
        reader.close();
    }

    public Token nextToken() {
        return iterator.next();
    }

    public boolean hasNextToken() {
        return iterator.hasNext();
    }

    public ArrayList<Token> getTable() {
        return table;
    }
}