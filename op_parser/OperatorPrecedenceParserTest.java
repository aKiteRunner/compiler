package op_parser;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperatorPrecedenceParserTest {
    @Test
    void showGrammar() {
        OperatorPrecedenceParser parser = new OperatorPrecedenceParser(new char[]{'#', '+', '*', '<', '>', 'i'});
        try {
            parser.parse("S->#E#");
            parser.parse("E->E+T");
            parser.parse("E->T");
            parser.parse("T->T*F");
            parser.parse("T->F");
            parser.parse("F-><E>");
            parser.parse("F->i");
            List<String> grammars = parser.showGrammar();
            List<String> strings = new ArrayList<>();
            strings.add("S->#E#");
            strings.add("T->T*F|F");
            strings.add("E->E+T|T");
            strings.add("F-><E>|i");
            assertLinesMatch(strings, grammars);
        } catch (ParseError error) {
            System.out.print("OperatorPrecedenceParser.parse error");
        }
    }

    @Test
    void parse() {
        OperatorPrecedenceParser parser = new OperatorPrecedenceParser(new char[]{'#', '+', '*', '<', '>', 'i'});
        try {
            Grammar grammar = parser.parse("  E->(E+T)  ");
            String correct = "E [(, E, +, T, )]";
            String ret = grammar.in.toString() + " " + grammar.out.toString();
            assertEquals(ret, correct);
        } catch (ParseError error) {
            System.out.print("OperatorPrecedenceParser.parse error");
        }
    }

}