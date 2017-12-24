package compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class Parser {
    private Lexer lexer;
    private FileWriter outFile;
    private SymbolTable table;
    private Interpreter interpreter;
    private List<String> errors;
    private Token token;
    // block, statement, factor的First集合
    private BitSet blockFirst, statementFirst, factorFirst;

    public Parser(Lexer lexer, String filename) throws IOException {
        this.lexer = lexer;
        outFile = new FileWriter(filename);
        table = new SymbolTable();
        interpreter = new Interpreter();
        errors = new ArrayList<>();
        blockFirst = new BitSet();
        statementFirst = new BitSet();
        factorFirst = new BitSet();

        // block = [ 'const' ident '=' number {',' ident '=' number} ';']
        // [ 'var' ident {',' ident} ';']
        // { 'procedure' ident ';' block ';' } statement
        blockFirst.set(Symbol.Const.ordinal());
        blockFirst.set(Symbol.Var.ordinal());
        blockFirst.set(Symbol.Procedure.ordinal());

        //statement = [ ident ':=' expression
        //        | 'call' ident
        //        | 'begin' statement {';' statement } 'end'
        //        | 'if' condition 'then' statement  ['else' statement]
        //        | 'while' condition 'do' statement
        //        | 'repeat' statement 'until' condition
        //        | 'read' '(' ident {',' ident} ')'
        //        | 'write' '(' ident {',' ident} ')']
        statementFirst.set(Symbol.Identifier.ordinal());
        statementFirst.set(Symbol.Call.ordinal());
        statementFirst.set(Symbol.Begin.ordinal());
        statementFirst.set(Symbol.If.ordinal());
        statementFirst.set(Symbol.While.ordinal());
        statementFirst.set(Symbol.Repeat.ordinal());
        statementFirst.set(Symbol.Read.ordinal());
        statementFirst.set(Symbol.Write.ordinal());

        // factor = ident | number | '(' expression ')'
        factorFirst.set(Symbol.Identifier.ordinal());
        factorFirst.set(Symbol.Integer.ordinal());
        factorFirst.set(Symbol.LeftParentheses.ordinal());
    }

    public void nextToken() {
        if (lexer.hasNextToken()) {
            token = lexer.nextToken();
        } else {
            token = null;
        }
    }

    public void parse() {
        BitSet nextLevel = new BitSet();
        nextLevel.or(blockFirst);
        nextLevel.or(statementFirst);
        nextLevel.set(Symbol.Period.ordinal());

        // 不以句号结尾, 报错
        if (token.symbol != Symbol.Period) {
            errors.add(String.format("In line %d, expected a period.", token.line));
        }
        
        table.printTable();
        interpreter.printInstructions();
    }
}