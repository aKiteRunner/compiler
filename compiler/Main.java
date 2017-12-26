package compiler;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        try {
            Lexer lexer = new Lexer("D:\\1234\\workspace\\java\\compiler\\src\\pl0_test\\correct_test5");
            lexer.lex();
            Interpreter interpreter = new Interpreter();
            Parser parser = new Parser(lexer, interpreter,"Pcode");
            parser.nextToken();
            parser.parse();
            interpreter.interpret(new BufferedReader(new InputStreamReader(System.in)), new BufferedWriter(new OutputStreamWriter(System.out)));
        } catch (IOException e) {
            System.out.println(e);
        } catch (CompileException e) {
            System.out.println(e.getErrors());
        }
    }
}
