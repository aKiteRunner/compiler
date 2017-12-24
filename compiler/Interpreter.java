package compiler;

import java.util.ArrayList;
import java.util.List;

public class Interpreter {
    // 运行栈上限
    private static final int stackSize = 1000;
    // Pcode 数组大小
    private static final int arraySize = 1000;
    public Instruction[] instructions;
    public int arrayPtr;

    public Interpreter() {
        this.instructions = new Instruction[arraySize];
        arrayPtr = 0;
    }

    public void generate(Code code, int level, int argument) throws ParseException {
        if (arrayPtr >= arraySize) {
            throw new ParseException("Program too large");
        }
        instructions[arrayPtr] = new Instruction(code, level, argument);
        arrayPtr++;
    }

    public void printInstructions(int start) {
        for (int i = start; i < arrayPtr; i++) {
            System.out.println(instructions[i]);
        }
        System.out.println();
    }
}
