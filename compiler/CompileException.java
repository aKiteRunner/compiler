package compiler;

import java.util.ArrayList;
import java.util.Collection;

public class CompileException extends Exception {
    private static final String[] errorInfo = new String[]{
            "",
            "应是=而不是:=", // 1
            "=后应为数字", // 2
            "常量标识符后应为=", // 3
            "const, var, procedure后应为标识符", // 4
            "缺少逗号或分号", // 5
            "过程说明后的符号不正确", // 6
            "应该为语句", // 7
            "程序体内语句后的符号不正确", // 8
            "缺少句号",// 9
            "应为句号", // 10
            "语句之间漏分号", // 11
            "未声明的标识符", // 12
            "不可向常量或过程名赋值", // 13
            "应为赋值运算符:=",  // 14
            "call后应为标识符", // 15
            "不可调用常量或变量", // 16
            "应为then", // 17
            "缺少分号", // 18
            "缺少do", //19
            "语句后的符号不正确", //20
            "应为关系运算符", // 21
            "表达式内不可有过程标识符", // 22
            "漏右括号", // 23
            "因子后不可为此符号", //24
            "表达式不能以此符号开始", //25
            "这个数太大", //26
            "不是合法的字符", //27
            "数越界", // 28
            "过程嵌套层数过大", // 29
            "缺少右括号", //30
            "缺少左括号", //31
            "read()中的变量未声明", //32
            "缺少语句", //33
            "向常量或者过程赋值", //34
            "无法输出过程的值" //35
    };
    private ArrayList<String> errors = new ArrayList<>();

    public CompileException(String message) {
        super(message);
        errors.add(message);
    }

    public CompileException() {
        super();
    }

    public CompileException(Collection<? extends String> strings) {
        super();
        errors.addAll(strings);
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public void addErrors(Collection<? extends String> collection) {
        errors.addAll(collection);
    }

    public void addErrors(int index, int line) {
        errors.add(String.format("Line %d: %s", line, errorInfo[index]));
    }

    public void addErrors(String message, int line) {
        errors.add(String.format("Line %d: %s", line, message));
    }
}
