package compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;

public class Parser {
    private Lexer lexer;
    private FileWriter outFile;
    private TokenTable table;
    private Interpreter interpreter;
    private CompileException errors;
    private Token token;
    // block, statement, factor的First集合
    private BitSet blockFirst, statementFirst, factorFirst;
    private int dx;

    public Parser(Lexer lexer, String filename) throws IOException {
        this.lexer = lexer;
        outFile = new FileWriter(filename);
        table = new TokenTable();
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
        System.out.println(token);
        if (lexer.hasNextToken()) {
            token = lexer.nextToken();
        } else {
            token = null;
        }
    }

    public boolean hasNextToken() {
        return lexer.hasNextToken();
    }

    public void parse() throws CompileException {
        BitSet nextLevel = new BitSet();
        nextLevel.or(blockFirst);
        nextLevel.or(statementFirst);
        nextLevel.set(Symbol.Period.ordinal());
        try {
            block(0, nextLevel);
        } catch (NullPointerException error) {
            errors.addErrors(Collections.singletonList("程序未正常结束"));
        }
        // 不以句号结尾, 报错
        if (token == null) {
            errors.addErrors(Collections.singletonList("缺少句号"));
        }
        else if (token.symbol != Symbol.Period) {
            errors.addErrors(9, token.line);
        }

        table.printTable();
        interpreter.printInstructions(0);
        if (errors.getErrors().size() != 0) {
            throw errors;
        } else {
            try {
                for (int i = 0; i < interpreter.arrayPtr; i++) {
                    outFile.write(interpreter.instructions[i].toString() + "\n");
                }
                outFile.close();
            } catch (IOException error) {
                System.err.println(error.getMessage());
            }
        }
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
        if (level > TokenTable.LEVEL_MAX) {
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
                } else {
                    errors.addErrors(5, token.line);
                }
            }
        } while (blockFirst.get(token.symbol.ordinal()));

        Item item = table.get(tablePtr);
        // 过程入口地址, JMP的第二个参数
        interpreter.instructions[item.address].argument = interpreter.arrayPtr;
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
        if (statementFirst.get(token.symbol.ordinal())) {
            statement(level, nextLevel);
        }
        // 分析完成后，生成指令， 用于从分程序返回（对于0层的主程序来说，就是程序运行完成，退出
        try {
            if (level != 0) {
                interpreter.generate(Code.EXP, 0, 0);
            } else {
                interpreter.generate(Code.HLT, 0, 0);
            }
        } catch (ParseException error) {
            errors.addErrors(error.getMessage(), token.line);
        }
        interpreter.printInstructions(interpreterPtr);
        dx = dx0;
        table.tablePtr = tablePtr;
    }

    private void statement(int level, BitSet follow) {
        switch (token.symbol) {
            case Identifier:
                assignmentStatement(level, follow);
                break;
            case Read:
                readStatement(level, follow);
                break;
            case Write:
                writeStatement(level, follow);
                break;
            case If:
                ifStatement(level, follow);
                break;
            case While:
                whileStatement(level, follow);
                break;
            case Repeat:
                repeatStatement(level, follow);
                break;
            case Begin:
                beginStatement(level, follow);
                break;
            case Call:
                callStatement(level, follow);
                break;
            default:
                BitSet nextLevel = new BitSet();
                test(follow, nextLevel, 20);
        }
    }

    private void constDeclare(int level) {
        // 'const' ident '=' number
        if (token.symbol == Symbol.Identifier) {
            String id = token.name;
            int index = table.index(id);
            //　查看是否有最近出现的一个和它重名, 且在同一层的变量,常量或者过程名
            if (index != 0 && table.get(index).level == level) {
                errors.addErrors(36, token.line);
            }
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
                        table.addConstant(token, level, value);
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
            int index = table.index(token.name);
            //　查看是否有最近出现的一个和它重名, 且在同一层的变量,常量或者过程名
            if (index != 0 && table.get(index).level == level) {
                errors.addErrors(36, token.line);
            }
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
            while (!s1.get(token.symbol.ordinal()) && hasNextToken()) {
                nextToken();
            }
        }
    }

    private void assignmentStatement(int level, BitSet follow) {
        // ident ':=' expression
        int index = table.index(token.name);
        if (index > 0) {
            Item item = table.get(index);
            if (item.type == Type.Variable) {
                nextToken();
                if (token.symbol == Symbol.Assign) {
                    nextToken();
                } else {
                    errors.addErrors(14, token.line);
                }
                BitSet nextLevel = (BitSet) follow.clone();
                // 解析表达式
                expression(level, nextLevel);
                try {
                    interpreter.generate(Code.STO, level - item.level, item.address);
                } catch (ParseException error) {
                    errors.addErrors(error.getMessage(), token.line);
                }
            } else {
                errors.addErrors(13, token.line);
            }
        } else {
            errors.addErrors(12, token.line);
        }
    }

    private void writeStatement(int level, BitSet follow) {
        nextToken();
        if (token.symbol == Symbol.LeftParentheses) {
            int index = 0;
            do {
                nextToken();
                if (token.symbol == Symbol.Identifier) {
                    index = table.index(token.name);
                }
                // 未声明的标识符
                if (index == 0) {
                    errors.addErrors(12, token.line);
                } else {
                    Item item = table.get(index);
                    if (item.type != Type.Procedure) {
                        try {
                            interpreter.generate(Code.WRT, 0, 0);
                        } catch (ParseException error) {
                            errors.addErrors(error.getMessage(), token.line);
                        }
                    } else {
                        // 输出过程的值
                        errors.addErrors(35, token.line);
                    }
                }
                nextToken();
            } while (token.symbol == Symbol.Comma);
        } else {
            errors.addErrors(31, token.line);
        }
        getRightParenthesis(follow);
        // 输出换行符
        try {
            interpreter.generate(Code.WRL, 0, 0);
        } catch (ParseException error) {
            errors.addErrors(error.getMessage(), token.line);
        }
    }

    private void getRightParenthesis(BitSet follow) {
        if (token.symbol == Symbol.RightParentheses) {
            nextToken();
        } else {
            errors.addErrors(30, token.line);
            while (!follow.get(token.symbol.ordinal()) && hasNextToken()) {
                nextToken();
            }
        }
    }

    private void readStatement(int level, BitSet follow) {
        nextToken();
        if (token.symbol == Symbol.LeftParentheses) {
            int index = 0;
            do {
                nextToken();
                if (token.symbol == Symbol.Identifier) {
                    index = table.index(token.name);
                }
                // 未声明的标识符
                if (index == 0) {
                    errors.addErrors(12, token.line);
                } else {
                    Item item = table.get(index);
                    if (item.type == Type.Variable) {
                        try {
                            interpreter.generate(Code.RED, 0, 0);
                            interpreter.generate(Code.STO, level - item.level, item.address);
                        } catch (ParseException error) {
                            errors.addErrors(error.getMessage(), token.line);
                        }
                    } else {
                        // 给常量或者过程赋值
                        errors.addErrors(34, token.line);
                    }
                }
                nextToken();
            } while (token.symbol == Symbol.Comma);
        } else {
            errors.addErrors(31, token.line);
        }
        getRightParenthesis(follow);
    }

    private void callStatement(int level, BitSet follow) {
        nextToken();
        if (token.symbol == Symbol.Identifier) {
            int index = table.index(token.name);
            if (index > 0) {
                Item item = table.get(index);
                switch (item.type) {
                    case Procedure:
                        try {
                            interpreter.generate(Code.CAL, level - item.level, item.address);
                        } catch (ParseException error) {
                            errors.addErrors(error.getMessage(), token.line);
                        }
                        break;
                    default:
                        errors.addErrors(16, token.line);
                }
            } else {
                // 未声明的标识符
                errors.addErrors(12, token.line);
            }
            nextToken();
        } else {
            // Call 后面应该接标识符
            errors.addErrors(15, token.line);
        }
    }

    private void ifStatement(int level, BitSet follow) {
        // 'if' condition 'then' statement  ['else' statement]
        nextToken();
        BitSet nextLevel = (BitSet) follow.clone();
        nextLevel.set(Symbol.Then.ordinal());
        condition(level, nextLevel);
        // Condition 后面接 Then
        if (token.symbol == Symbol.Then) {
            nextToken();
        } else {
            errors.addErrors(17, token.line);
        }
        int interpreterPtr1 = interpreter.arrayPtr;
        try {
            interpreter.generate(Code.JPC, 0, 0);
        } catch (ParseException error) {
            errors.addErrors(error.getMessage(), token.line);
        }
        nextLevel.set(Symbol.Else.ordinal());
        statement(level, nextLevel);
        if (token.symbol == Symbol.Else) {
            // 如果有 Else 则需要增加一条跳转命令
            int interpreter2 = interpreter.arrayPtr;
            try {
                interpreter.generate(Code.JMP, 0, 0);
            } catch (ParseException error) {
                errors.addErrors(error.getMessage(), token.line);
            }
            // 回填Condition为假时, 即进入Else的地址
            interpreter.instructions[interpreterPtr1].argument = interpreter.arrayPtr;
            nextToken();
            statement(level, follow);
            // 回填Condition为真时, 跳过Else分支的地址
            interpreter.instructions[interpreter2].argument = interpreter.arrayPtr;
        } else {
            interpreter.instructions[interpreterPtr1].argument = interpreter.arrayPtr;
        }
    }

    private void beginStatement(int level, BitSet follow) {
        nextToken();
        BitSet nextLevel = (BitSet) follow.clone();
        nextLevel.set(Symbol.SemiColon.ordinal());
        nextLevel.set(Symbol.End.ordinal());
        // 递归statement
        statement(level, nextLevel);
        while (statementFirst.get(token.symbol.ordinal()) || token.symbol == Symbol.SemiColon) {
            // 判断是否缺少分号
            if (token.symbol == Symbol.SemiColon) {
                nextToken();
            } else {
                errors.addErrors(11, token.line);
            }
            statement(level, nextLevel);
        }
        if (token.symbol == Symbol.End) {
            nextToken();
        } else {
            errors.addErrors(18, token.line);
        }
    }

    private void whileStatement(int level, BitSet follow) {
        // 循环开始
        int interpreterPtr1 = interpreter.arrayPtr;
        nextToken();
        BitSet nextLevel = (BitSet) follow.clone();
        nextLevel.set(Symbol.Do.ordinal());
        condition(level, nextLevel);
        // 循环结束
        int interpreterPtr2 = interpreter.arrayPtr;
        try {
            interpreter.generate(Code.JPC, 0, 0);
        } catch (ParseException error) {
            errors.addErrors(error.getMessage(), token.line);
        }
        // Condition后面接Do
        if (token.symbol == Symbol.Do) {
            nextToken();
        } else {
            errors.addErrors(19, token.line);
        }
        statement(level, follow);
        // 跳回条件判断的位置
        try {
            interpreter.generate(Code.JMP, 0, interpreterPtr1);
        } catch (ParseException error) {
            errors.addErrors(error.getMessage(), token.line);
        }
        // 回填跳出循环的地址
        interpreter.instructions[interpreterPtr2].argument = interpreter.arrayPtr;
    }

    private void repeatStatement(int level, BitSet follow) {
        // 'repeat' statement 'until' condition
        int interpreterPtr1 = interpreter.arrayPtr;
        nextToken();
        BitSet nextLevel = (BitSet) follow.clone();
        nextLevel.set(Symbol.Until.ordinal());
        statement(level, nextLevel);
        // 如果是多个statement, 虽然我觉得文法有问题
//        while (statementFirst.get(token.symbol.ordinal()) || token.symbol == Symbol.SemiColon) {
//            if (token.symbol == Symbol.SemiColon) {
//                nextToken();
//            } else {
//                // 缺少分号
//                errors.addErrors(18, token.line);
//            }
//            statement(level, nextLevel);
//        }
        // Statement 后接 Until
        if (token.symbol == Symbol.Until) {
            nextToken();
            condition(level, follow);
            try {
                interpreter.generate(Code.JPC, 0, interpreterPtr1);
            } catch (ParseException error) {
                errors.addErrors(error.getMessage(), token.line);
            }
        }
    }

    private void expression(int level, BitSet follow) {
        // expression = [ '+'|'-'] term { ('+'|'-') term}
        if (token.symbol == Symbol.Plus || token.symbol == Symbol.Minus) {
            Symbol operator = token.symbol;
            nextToken();
            BitSet nextLevel = (BitSet) follow.clone();
            nextLevel.set(Symbol.Plus.ordinal());
            nextLevel.set(Symbol.Minus.ordinal());
            term(level, nextLevel);
            // Neg 取反
            if (operator == Symbol.Minus) {
                try {
                    interpreter.generate(Code.MUS, 0, 0);
                } catch (ParseException error) {
                    errors.addErrors(error.getMessage(), token.line);
                }
            }
        } else {
            BitSet nextLevel = (BitSet) follow.clone();
            nextLevel.set(Symbol.Plus.ordinal());
            nextLevel.set(Symbol.Minus.ordinal());
            term(level, nextLevel);
        }
        // { ('+'|'-') term}
        while (token.symbol == Symbol.Plus || token.symbol == Symbol.Minus) {
            Symbol operator = token.symbol;
            nextToken();
            BitSet nextLevel = (BitSet) follow.clone();
            nextLevel.set(Symbol.Plus.ordinal());
            nextLevel.set(Symbol.Minus.ordinal());
            term(level, nextLevel);
            try {
                // 2, 3分别为加减法
                if (operator == Symbol.Plus) {
                    interpreter.generate(Code.ADD, 0, 0);
                } else {
                    interpreter.generate(Code.SUB, 0, 0);
                }
            } catch (ParseException error) {
                errors.addErrors(error.getMessage(), token.line);
            }
        }
    }

    private void term(int level, BitSet follow) {
        // term = factor {('*'|'/') factor}
        BitSet nextLevel = (BitSet) follow.clone();
        nextLevel.set(Symbol.Star.ordinal());
        nextLevel.set(Symbol.Slash.ordinal());
        factor(level, nextLevel);
        while (token.symbol == Symbol.Star || token.symbol == Symbol.Slash) {
            Symbol operator = token.symbol;
            nextToken();
            factor(level, nextLevel);
            try {
                if (operator == Symbol.Star) {
                    interpreter.generate(Code.MUL, 0, 0);
                } else {
                    interpreter.generate(Code.DIV, 0, 0);
                }
            } catch (ParseException error) {
                errors.addErrors(error.getMessage(), level);
            }
        }
    }

    private void factor(int level, BitSet follow) {
        // factor = ident | number | '(' expression ')'
        // 以Identifier, Number, ( 开始
        if (token.symbol == Symbol.Identifier) {
            int index = table.index(token.name);
            if (index > 0) {
                Item item = table.get(index);
                switch (item.type) {
                    case Variable:
                        try {
                            interpreter.generate(Code.LOD, level - item.level, item.address);
                        } catch (ParseException error) {
                            errors.addErrors(error.getMessage(), token.line);
                        }
                        break;
                    case Constant:
                        try {
                            interpreter.generate(Code.LDC, 0, item.value);
                        } catch (ParseException error) {
                            errors.addErrors(error.getMessage(), token.line);
                        }
                        break;
                    case Procedure:
                        // 表达式中不可以有过程
                        errors.addErrors(22, token.line);
                }
            } else {
                // 未声明的标识符
                errors.addErrors(12, token.line);
            }
            nextToken();
        } else if (token.symbol == Symbol.Integer) {
            int value = Integer.parseInt(token.name);
            try {
                interpreter.generate(Code.LDC, 0, value);
            } catch (ParseException error) {
                errors.addErrors(error.getMessage(), token.line);
            }
            nextToken();
        } else if (token.symbol == Symbol.LeftParentheses) {
            nextToken();
            BitSet nextLevel = (BitSet) follow.clone();
            nextLevel.set(Symbol.RightParentheses.ordinal());
            expression(level, nextLevel);
            if (token.symbol == Symbol.RightParentheses) {
                nextToken();
            } else {
                errors.addErrors(30, token.line);
            }
        } else {
            errors.addErrors(24, token.line);
        }
    }

    private void condition(int level, BitSet follow) {
        // condition = 'odd' expression |
        //            expression ('='|'#'|'<'|'<='|'>'|'>=') expression
        if (token.symbol == Symbol.Odd) {
            nextToken();
            expression(level, follow);
            try {
                interpreter.generate(Code.ODD, 0, 0);
            } catch (ParseException error) {
                errors.addErrors(error.getMessage(), token.line);
            }
        } else {
            // expression ('='|'#'|'<'|'<='|'>'|'>=') expression
            BitSet nextLevel = (BitSet) follow.clone();
            nextLevel.set(Symbol.Equal.ordinal());
            nextLevel.set(Symbol.Unequal.ordinal());
            nextLevel.set(Symbol.Greater.ordinal());
            nextLevel.set(Symbol.GreaterEqual.ordinal());
            nextLevel.set(Symbol.Less.ordinal());
            nextLevel.set(Symbol.LessEqual.ordinal());
            expression(level, nextLevel);
            switch (token.symbol) {
                case Equal:
                    nextToken();
                    expression(level, follow);
                    try {
                        interpreter.generate(Code.EQL, 0, 0);
                    } catch (ParseException error) {
                        errors.addErrors(error.getMessage(), token.line);
                    }
                    break;
                case Unequal:
                    nextToken();
                    expression(level, follow);
                    try {
                        interpreter.generate(Code.NEQ, 0, 0);
                    } catch (ParseException error) {
                        errors.addErrors(error.getMessage(), token.line);
                    }
                    break;
                case Greater:
                    nextToken();
                    expression(level, follow);
                    try {
                        interpreter.generate(Code.GRT, 0, 0);
                    } catch (ParseException error) {
                        errors.addErrors(error.getMessage(), token.line);
                    }
                    break;
                case GreaterEqual:
                    nextToken();
                    expression(level, follow);
                    try {
                        interpreter.generate(Code.GEQ, 0, 0);
                    } catch (ParseException error) {
                        errors.addErrors(error.getMessage(), token.line);
                    }
                    break;
                case Less:
                    nextToken();
                    expression(level, follow);
                    try {
                        interpreter.generate(Code.LSS, 0, 0);
                    } catch (ParseException error) {
                        errors.addErrors(error.getMessage(), token.line);
                    }
                    break;
                case LessEqual:
                    nextToken();
                    expression(level, follow);
                    try {
                        interpreter.generate(Code.LER, 0, 0);
                    } catch (ParseException error) {
                        errors.addErrors(error.getMessage(), token.line);
                    }
                    break;
                default:
                    errors.addErrors(21, token.line);
            }
        }
    }
}