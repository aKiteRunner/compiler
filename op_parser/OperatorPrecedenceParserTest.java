package op_parser;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperatorPrecedenceParserTest {
    @Test
    void firstVT() {
        OperatorPrecedenceParser parser = new OperatorPrecedenceParser(new char[]{'#', '+', '*', '<', '>', 'i'});
        try {
            String[] strings = new String[] {
                    "S->#E#",
                    "E->E+T",
                    "E->T",
                    "T->T*F",
                    "T->F",
                    "F-><E>",
                    "F->i",
            };
            parser.parse(Arrays.asList(strings));
            List<String> grammars = parser.showGrammar();
            String[] correct = new String[] {
                    "S->#E#",
                    "T->T*F|F",
                    "E->E+T|T",
                    "F-><E>|i"
            };

            TerminalTerm[] expect = new TerminalTerm[]{
                    new TerminalTerm('#')
            };
            assertArrayEquals(expect, parser.firstVT(new NotTerminalTerm('S')).toArray());

            expect = new TerminalTerm[]{
                    new TerminalTerm('i'),
                    new TerminalTerm('*'),
                    new TerminalTerm('+'),
                    new TerminalTerm('<')
            };
            assertArrayEquals(expect, parser.firstVT(new NotTerminalTerm('E')).toArray());

            expect = new TerminalTerm[]{
                    new TerminalTerm('i'),
                    new TerminalTerm('*'),
                    new TerminalTerm('<')
            };
            assertArrayEquals(expect, parser.firstVT(new NotTerminalTerm('T')).toArray());

            expect = new TerminalTerm[]{
                    new TerminalTerm('i'),
                    new TerminalTerm('<')
            };
            assertArrayEquals(expect, parser.firstVT(new NotTerminalTerm('F')).toArray());
        } catch (ParseError error) {
            System.out.print("OperatorPrecedenceParser.parse error");
        }
    }

    @Test
    void lastVT() {
        OperatorPrecedenceParser parser = new OperatorPrecedenceParser(new char[]{'#', '+', '*', '<', '>', 'i'});
        try {
            String[] strings = new String[] {
                    "S->#E#",
                    "E->E+T",
                    "E->T",
                    "T->T*F",
                    "T->F",
                    "F-><E>",
                    "F->i",
            };
            parser.parse(Arrays.asList(strings));
            List<String> grammars = parser.showGrammar();
            String[] correct = new String[] {
                    "S->#E#",
                    "T->T*F|F",
                    "E->E+T|T",
                    "F-><E>|i"
            };

            TerminalTerm[] expect = new TerminalTerm[]{
                    new TerminalTerm('#')
            };
            assertArrayEquals(expect, parser.lastVT(new NotTerminalTerm('S')).toArray());

            expect = new TerminalTerm[]{
                    new TerminalTerm('i'),
                    new TerminalTerm('*'),
                    new TerminalTerm('+'),
                    new TerminalTerm('>')
            };
            assertArrayEquals(expect, parser.lastVT(new NotTerminalTerm('E')).toArray());

            expect = new TerminalTerm[]{
                    new TerminalTerm('i'),
                    new TerminalTerm('*'),
                    new TerminalTerm('>')
            };
            assertArrayEquals(expect, parser.lastVT(new NotTerminalTerm('T')).toArray());

            expect = new TerminalTerm[]{
                    new TerminalTerm('i'),
                    new TerminalTerm('>')
            };
            assertArrayEquals(expect, parser.lastVT(new NotTerminalTerm('F')).toArray());
        } catch (ParseError error) {
            System.out.print("OperatorPrecedenceParser.parse error");
        }
    }

    @Test
    void showGrammar() {
        OperatorPrecedenceParser parser = new OperatorPrecedenceParser(new char[]{'#', '+', '*', '<', '>', 'i'});
        try {
            String[] strings = new String[] {
                    "S->#E#",
                    "E->E+T",
                    "E->T",
                    "T->T*F",
                    "T->F",
                    "F-><E>",
                    "F->i",
            };
            parser.parse(Arrays.asList(strings));
            List<String> grammars = parser.showGrammar();
            String[] correct = new String[] {
                    "S->#E#",
                    "T->T*F|F",
                    "E->E+T|T",
                    "F-><E>|i"
            };
            assertLinesMatch(Arrays.asList(correct), grammars);
        } catch (ParseError error) {
            System.out.print("OperatorPrecedenceParser.parse error");
        }
    }
}