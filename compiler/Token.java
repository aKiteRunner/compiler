package compiler;

import java.util.HashMap;

public class Token {
    private static final HashMap<String, Symbol> MAP = new HashMap<String, Symbol>(){{
        put("const", Symbol.Const);
        put("var", Symbol.Var);
        put("odd", Symbol.Odd);
        put("begin", Symbol.Begin);
        put("end", Symbol.End);
        put("if", Symbol.If);
        put("then", Symbol.Then);
        put("else", Symbol.Else);
        put("while", Symbol.While);
        put("do", Symbol.Do);
        put("call", Symbol.Call);
        put("procedure", Symbol.Procedure);
        put("write", Symbol.Write);
        put("read", Symbol.Read);
        put("repeat", Symbol.Repeat);
        put("until", Symbol.Until);
    }};

    public static Symbol getKeywordSymbol(String keyword) {
        return MAP.getOrDefault(keyword, Symbol.None);
    }

    public static Type getSymbolType(Symbol symbol) {
        switch (symbol) {
            case Var:
                return Type.Variable;
            case Const:
                return Type.Constant;
            case Procedure:
                return Type.Procedure;
            default :
                return Type.None;
        }
    }

    public Symbol symbol;
    public String name;
    public int line;

    public Token(String name, Symbol symbol, int line) {
        this.name = name;
        this.symbol = symbol;
        this.line = line;
    }

    @Override
    public String toString() {
        return String.format("symbol: %s, name: %s, line: %d", symbol, name, line);
    }
}

enum Symbol {
    None, Identifier, Integer, Float, SemiColon, Comma, Plus, Minus, Star, Slash, LeftParentheses, RightParentheses,
    Assign, Less, Equal, LessEqual, Greater, GreaterEqual, Unequal, Period, Odd, Begin, End, If, Then, Else,
    While, Do, Call, Const, Var, Procedure, Write, Read, Repeat, Until
}