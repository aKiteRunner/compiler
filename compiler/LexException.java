package compiler;

import java.util.Collection;

class LexException extends CompileException {
    public LexException(String message) {
        super(message);
    }

    public LexException() {
        super();
    }

    public LexException(Collection<? extends String> strings) {
        super(strings);
    }
}
