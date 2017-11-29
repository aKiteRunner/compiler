import org.junit.jupiter.api.Test;

import java.io.IOException;

class LexerTest {
    @Test
    void parse() {
        try {
            Lexer lexer = new Lexer("pl0_test");
            System.out.print("Parse start\n");
            lexer.lex();
            System.out.print("Parse complete\n");
            for (Token t: lexer.table
                 ) {
                System.out.print(t.name + "\t" + t.symbol + "\n");
            }
        } catch (IOException | LexException e) {
            System.out.print(e);
        }
    }
}