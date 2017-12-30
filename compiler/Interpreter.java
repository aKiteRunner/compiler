package compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

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

    private int base(int l, int b, int[] runtimeStack) {
        // 通过给定的层次差来获得该层的堆栈帧基址
        while (l > 0) {
            b = runtimeStack[b];
            l--;
        }
        return b;
    }

    public void interpret(BufferedReader in, BufferedWriter out) {
        int[] runtimeStack = new int[stackSize];
        int pc = 0, bp = 0, sp = -1;
        Instruction instruction;
        Scanner scanner = new Scanner(in);
        do {
            instruction = instructions[pc++];
            switch (instruction.code) {
                case LIT:
                    // 加载某个数值
                    ++sp;
                    runtimeStack[sp] = instruction.argument;
                    break;
                case LOD:
                    // 加载某个标识符对应的值
                    ++sp;
                    runtimeStack[sp] = runtimeStack[base(instruction.level, bp, runtimeStack) + instruction.argument];
                    break;
                case STO:
                    // 保存某个标识符
                    runtimeStack[base(instruction.level, bp, runtimeStack) + instruction.argument] = runtimeStack[sp];
                    sp--;
                    break;
                case CAL:
                    runtimeStack[sp + 1] = base(instruction.level, bp, runtimeStack);
                    runtimeStack[sp + 2] = bp;
                    runtimeStack[sp + 3] = pc;
                    bp = sp + 1;
                    pc = instruction.argument;
                    break;
                case INT:
                    sp += instruction.argument;
                    break;
                case JMP:
                    pc = instruction.argument;
                    break;
                case JPC:
                    if (runtimeStack[sp] == 0) {
                        pc = instruction.argument;
                    }
                    sp--;
                    break;
                case OPR:
                    switch (instruction.argument) {
                        case 0:
                            sp = bp - 1;
                            bp = runtimeStack[sp + 2];
                            pc = runtimeStack[sp + 3];
                            break;
                        case 1:
                            runtimeStack[sp] = -runtimeStack[sp];
                            break;
                        case 2:
                            runtimeStack[sp - 1] += runtimeStack[sp];
                            sp--;
                            break;
                        case 3:
                            runtimeStack[sp - 1] -= runtimeStack[sp];
                            sp--;
                            break;
                        case 4:
                            runtimeStack[sp - 1] *= runtimeStack[sp];
                            sp--;
                            break;
                        case 5:
                            runtimeStack[sp - 1] /= runtimeStack[sp];
                            sp--;
                            break;
                        case 6:
                            runtimeStack[sp] %= 2;
                            break;
                        case 8:
                            runtimeStack[sp - 1] = runtimeStack[sp - 1] == runtimeStack[sp] ? 1 : 0;
                            sp--;
                            break;
                        case 9:
                            runtimeStack[sp - 1] = runtimeStack[sp - 1] != runtimeStack[sp] ? 1 : 0;
                            sp--;
                            break;
                        case 10:
                            runtimeStack[sp - 1] = runtimeStack[sp - 1] < runtimeStack[sp] ? 1 : 0;
                            sp--;
                            break;
                        case 11:
                            runtimeStack[sp - 1] = runtimeStack[sp - 1] <= runtimeStack[sp] ? 1 : 0;
                            sp--;
                            break;
                        case 12:
                            runtimeStack[sp - 1] = runtimeStack[sp - 1] > runtimeStack[sp] ? 1 : 0;
                            sp--;
                            break;
                        case 13:
                            runtimeStack[sp - 1] = runtimeStack[sp - 1] >= runtimeStack[sp] ? 1 : 0;
                            sp--;
                            break;
                    }
                    break;
                case WRT:
                    try {
                        out.write(Integer.toString(runtimeStack[sp]) + "\n");
                        out.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    sp--;
                    break;
                case RED:
                    sp++;
                    runtimeStack[sp] = scanner.nextInt();
                    runtimeStack[base(instruction.level, bp, runtimeStack) + instruction.argument] = runtimeStack[sp];
                    sp--;
                    break;
            }
        } while (pc != 0);
    }

    public List<Instruction> getInstructions() {
        List<Instruction> res = new ArrayList<>(arrayPtr);
        res.addAll(Arrays.asList(instructions).subList(0, arrayPtr));
        return res;
    }
}
