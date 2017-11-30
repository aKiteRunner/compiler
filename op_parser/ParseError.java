package op_parser;

public class ParseError extends Exception {
    public ParseError() {
        super();
    }

    public ParseError(String message) {
        super(message);
    }
}
