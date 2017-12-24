package compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

public class Parser {
    private Lexer lexer;
    private FileWriter outFile;
    private SymbolTable table;
    private Interpreter interpreter;
    private CompileException errors;
    private Token token;
    // block, statement, factor的First集合
    private BitSet blockFirst, statementFirst, factorFirst;
    private int dx;

    public Parser(Lexer lexer, String filename) throws IOException {
        this.lexer = lexer;
        outFile = new FileWriter(filename);
        table = new SymbolTable();
        interpreter = new Interpreter();
        errors = new CompileException();
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

        block(0, nextLevel);

        // 不以句号结尾, 报错
        if (token.symbol != Symbol.Period) {
            errors.addErrors(9, token.line);
        }

        table.printTable();
        interpreter.printInstructions(0);
    }

    private void block(int level, BitSet follow) {
        // block = [ 'const' ident '=' number {',' ident '=' number} ';']
        // [ 'var' ident {',' ident} ';']
        // { 'procedure' ident ';' block ';' } statement
        BitSet nextLevel;
        // 记录进入该层之前的变量值, 最后恢复
        int dx0 = dx, tablePtr = table.tablePtr, interpreterPtr;
        //每一层最开始的位置有三个空间用于存放静态链SL、动态链DL和返回地址RA
        dx = 3;
        // Pcode代码的地址
        table.get(tablePtr).address = interpreter.arrayPtr;
        try {
            interpreter.generate(Code.JMP, 0, 0);
        } catch (ParseException error) {
            errors.addErrors(error.getMessage(), token.line);
        }
        //嵌套层数过大
        if (level > SymbolTable.LEVEL_MAX) {
            errors.addErrors(29, token.line);
        }
        do {
            // 'const' ident '=' number {',' ident '=' number} ';'
            if (token.symbol == Symbol.Const) {
                nextToken();
                constDeclare(level);
                while (token.symbol == Symbol.Comma) {
                    nextToken();
                    constDeclare(level);
                }
                // 判断是否以分号结束, 否则报错
                if (token.symbol == Symbol.SemiColon) {
                    nextToken();
                } else {
                    errors.addErrors(5, token.line);
                }
            }
            // 'var' ident {',' ident} ';'
            if (token.symbol == Symbol.Var) {
                nextToken();
                variableDeclare(level);
                while (token.symbol == Symbol.Comma) {
                    nextToken();
                    variableDeclare(level);
                }
                if (token.symbol == Symbol.SemiColon) {
                    nextToken();
                } else {
                    errors.addErrors(5, token.line);
                }
            }
            // { 'procedure' ident ';' block ';' } statement
            while (token.symbol == Symbol.Procedure) {
                nextToken();
                // 过程名
                if (token.symbol == Symbol.Identifier) {
                    try {
                        table.addProcedure(token, level);
                    } catch (ParseException error) {
                        errors.addErrors(error.getMessage(), token.line);
                    }
                    nextToken();
                } else {
                    errors.addErrors(4, token.line);
                }
                // 过程名后面接分号
                if (token.symbol == Symbol.SemiColon) {
                    nextToken();
                } else {
                    errors.addErrors(5, token.line);
                }
                nextLevel = (BitSet) follow.clone();
                nextLevel.set(Symbol.SemiColon.ordinal());
                block(level + 1, nextLevel);
                // 过程后面接分号
                if (token.symbol == Symbol.SemiColon) {
                    nextToken();
                    // 进入statement
                    nextLevel = (BitSet) statementFirst.clone();
                    nextLevel.set(Symbol.Procedure.ordinal());
                    test(nextLevel, follow, 6);
                } else {
                    errors.addErrors(5, token.line);
                }
            }
            nextLevel = (BitSet) statementFirst.clone();
            test(nextLevel, blockFirst, 7);
        } while (blockFirst.get(token.symbol.ordinal()));

        Item item = table.get(tablePtr);
        // 过程入口地址, JMP的第二个参数
        interpreter.instructions.get(item.address).argument = interpreter.arrayPtr;
        item.address = interpreter.arrayPtr;
        item.size = dx;
        // dx即是当前堆栈大小
        interpreterPtr = interpreter.arrayPtr;
        // 分配内存
        try {
            interpreter.generate(Code.INT, 0, dx);
        } catch (ParseException error) {
            errors.addErrors(error.getMessage(), token.line);
        }
        table.printTable();
        // statement 部分
        nextLevel = (BitSet) follow.clone();
        nextLevel.set(Symbol.SemiColon.ordinal());
        nextLevel.set(Symbol.End.ordinal());
        statement(level, nextLevel);
        // 分析完成后，生成操作数为0的opr指令， 用于从分程序返回（对于0层的主程序来说，就是程序运行完成，退出
        try {
            interpreter.generate(Code.OPR, 0, 0);
        } catch (ParseException error) {
            errors.addErrors(error.getMessage(), token.line);
        }
        nextLevel = new BitSet();
        test(follow, nextLevel, 8);
        interpreter.printInstructions(interpreterPtr);
        dx = dx0;
        table.tablePtr = tablePtr;

    }

    private void statement(int level, BitSet follow) {

    }

    private void constDeclare(int level) {
        // 'const' ident '=' number
        if (token.symbol == Symbol.Identifier) {
            String id = token.name;
            nextToken();
            if (token.symbol == Symbol.Equal || token.symbol == Symbol.Assign) {
                // 如果赋值和等于号写反
                if (token.symbol == Symbol.Assign) {
                    errors.addErrors(1, token.line);
                }
                nextToken();
                if (token.symbol == Symbol.Integer) {
                    int value = Integer.parseInt(token.name);
                    token.name = id;
                    try {
                        table.addConstant(token, value);
                    } catch (ParseException error) {
                        errors.addErrors(error.getMessage(), token.line);
                    }
                    nextToken();
                } else {
                    // = 后面应该接的是数字
                    errors.addErrors(2, token.line);
                }
            } else {
                // Identity 后面接的应该是 =
                errors.addErrors(3, token.line);
            }
        } else {
            // Const 后面应该接的是 Identity
            errors.addErrors(4, token.line);
        }
    }

    private void variableDeclare(int level) {
        // 'var' ident
        if (token.symbol == Symbol.Identifier) {
            try {
                table.addVariable(token, level, dx);
                dx++;
            } catch (ParseException error) {
                errors.addErrors(error.getMessage(), token.line);
            }
            nextToken();
        } else {
            // Var 后面应该接的是 Identity
            errors.addErrors(4, token.line);
        }
    }

    private void test(BitSet s1, BitSet s2, int errorCode) {
        if (!s1.get(token.symbol.ordinal())) {
            errors.addErrors(errorCode, token.line);
            s1.or(s2);
            while (!s1.get(token.symbol.ordinal())) {
                nextToken();
            }
        }
    }
}