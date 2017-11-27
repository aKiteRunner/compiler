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
            for (int i = 0; i < parser.words.size(); i++) {
                System.out.println(parser.symbols.get(i) + "\t" + parser.words.get(i));
            }
        } catch (IOException | ParseException e) {
            System.out.print(e);
        }
    }
}