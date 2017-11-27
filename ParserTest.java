import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test
    void parse() {
        try {
            Parser parser = new Parser("pl0_test");
            System.out.print("Parse start\n");
            parser.parse();
            System.out.print("Parse complete\n");
            for (Token t: parser.table
                 ) {
                System.out.print(t.token + "\t" + t.symbol + "\n");
            }
        } catch (IOException | ParseException e) {
            System.out.print(e);
        }
    }
}