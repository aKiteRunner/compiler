package compiler;

public class Instruction {
    public int level, argument;
    public Code code;

    public Instruction(Code code, int level, int argument) {
        this.code = code;
        this.level = level;
        this.argument = argument;
    }

    @Override
    public String toString() {
        return String.format("code: %s, level: %d, argument: %d", code, level, argument);
    }
}

enum Code {
    LIT, OPR, LOD, STO, CAL, INT ,JMP, JPC
}