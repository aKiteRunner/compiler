package op_parser;

import com.sun.org.apache.regexp.internal.RE;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperatorPrecedenceParserTest {
    @Test
    void parse() {
        OperatorPrecedenceParser parser = new OperatorPrecedenceParser(new char[]{'#', '+', '*', '<', '>', 'i'});
        String[] strings = new String[] {
                "S->#E#",
                "E->E+T",
                "E->T",
                "T->T*F",
                "T->F",
                "F-><E>",
                "F->i",
        };
        try {
            parser.parse(Arrays.asList(strings));
        } catch (ParseError e) {
            fail("Parse error");
        }
    }

    @Test
    void table() {
        OperatorPrecedenceParser parser = new OperatorPrecedenceParser(new char[]{'#', '+', '*', '<', '>', 'i'});
        String[] strings = new String[] {
                "S->#E#",
                "E->E+T",
                "E->T",
                "T->T*F",
                "T->F",
                "F-><E>",
                "F->i",
        };
        try {
            parser.parse(Arrays.asList(strings));
            Relation[][] expect = new Relation[][]{
                    // #  i  *  +  <  >
                    new Relation[]{
                            Relation.Equal, Relation.Less, Relation.Less, Relation.Less, Relation.Less, Relation.None
                    },
                    new Relation[]{
                            Relation.Greater, Relation.None, Relation.Greater, Relation.Greater, Relation.None, Relation.Greater
                    },
                    new Relation[]{
                            Relation.Greater, Relation.Less, Relation.Greater, Relation.Greater, Relation.Less, Relation.Greater
                    },
                    new Relation[]{
                            Relation.Greater, Relation.Less, Relation.Less, Relation.Greater, Relation.Less, Relation.Greater
                    },
                    new Relation[]{
                            Relation.None, Relation.Less, Relation.Less, Relation.Less, Relation.Less, Relation.Equal
                    },
                    new Relation[]{
                            Relation.Greater, Relation.None, Relation.Greater, Relation.Greater, Relation.None, Relation.Greater
                    }
            };
            Relation[][] actual = parser.table();
            assertEquals(expect.length, actual.length);
            for (int i = 0; i < expect.length; i++) {
                assertArrayEquals(expect[i], actual[i]);
            }
        } catch (ParseError e) {
            fail("Parse error");
        }
    }

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
            fail("Parse error");
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
            fail("Parse error");
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
            fail("Parse error");
        }
    }
}