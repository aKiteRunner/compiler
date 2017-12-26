package compiler;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Lexer lexer = new Lexer("D:\\1234\\workspace\\java\\compiler\\src\\pl0_test\\correct_test4");
            lexer.lex();
            Parser parser = new Parser(lexer, "Pcode");
            parser.nextToken();
            parser.parse();
        } catch (IOException e) {
            System.out.println(e);
        } catch (CompileException e) {
            System.out.println(e.getErrors());
        }
    }
}
