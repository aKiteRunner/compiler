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

    public Token() {

    }

    public Token(String token, Symbol symbol) {
        this.token = token;
        this.symbol = symbol;
    }

    public Symbol symbol;
    public String token;
}

enum Symbol {
    None, Identifier, Integer, Float, SemiColon, Comma, Plus, Minus, Star, Divide, LeftParentheses, RightParentheses,
    Assign, Less, Equal, LessEqual, Greater, GreaterEqual, Unequal, Period, Odd, Begin, End, If, Then, Else,
    While, Do, Call, Const, Var, Procedure, Write, Read, Repeat, Until
}