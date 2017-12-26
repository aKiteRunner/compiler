package web;

import compiler.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.stream.Collectors;

@RestController
public class CompilerController {
    private Parser parser;
    private Lexer lexer;
    private Interpreter interpreter;
    @PostMapping ("/compile")
    public CompileOutput compile(@RequestBody String code) {
        // 返回编译结果
        CompileOutput output = new CompileOutput();
        System.out.println(code);
        try {
            lexer = new Lexer(new BufferedReader(new StringReader(code)));
            interpreter = new Interpreter();
            parser = new Parser(lexer, interpreter, "pl0.tmp");
            lexer.lex();
            parser.nextToken();
            parser.parse();
            output.setInstructions(interpreter.getInstructions().stream().map(Instruction::toString).collect(Collectors.toList()));
        } catch (IOException error) {
            System.out.println(error.getMessage());
        } catch (CompileException error) {
            output.setErrors(error.getErrors());
        }
        return output;
    }

    @PostMapping ("/run")
    public RunOutput run(@RequestBody String input) {
        StringWriter res = new StringWriter();
        interpreter.interpret(new BufferedReader(new StringReader(input)), new BufferedWriter(res));
        RunOutput output = new RunOutput();
        output.setOutput(res.toString());
        return output;
    }
}
