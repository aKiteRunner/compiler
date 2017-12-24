package compiler;

import java.util.ArrayList;
import java.util.Collection;

public class CompileException extends Exception {
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
}
