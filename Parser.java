import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<Token> tokenList;
    private FileWriter outFile;

    public Parser(List<Token> tokenList, String filename) throws IOException {
        this.tokenList = tokenList;
        outFile = new FileWriter(filename);
    }

    public void parse() {

    }
}