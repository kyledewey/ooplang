package ooplang.parser;

import ooplang.tokenizer.*;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ParserTest {
    public static void assertLhsParses(final String string) throws TokenizerException, ParseException {
        new Parser(Tokenizer.tokenize(string)).parseLhs(0);
    }

    @Test
    public void variableIsLhs() throws TokenizerException, ParseException {
        assertLhsParses("x");
    }

    @Test
    public void thisAccessIsLhs() throws TokenizerException, ParseException {
        assertLhsParses("(. this x)");
    }

    @Test
    public void variableAccessIsLhs() throws TokenizerException, ParseException {
        assertLhsParses("(. x y)");
    }

    @Test
    public void lhsAccessIsLhs() throws TokenizerException, ParseException {
        assertLhsParses("(. (. this x) y)");
    }

    public static void assertOptionalSuperParses(final String string) throws TokenizerException, ParseException {
        new Parser(Tokenizer.tokenize(string)).parseOptionalSuper(0);
    }

    @Test
    public void assertNonSuperParses() throws TokenizerException, ParseException {
        assertOptionalSuperParses("");
    }

    @Test
    public void assertSuperNoArgsParses() throws TokenizerException, ParseException {
        assertOptionalSuperParses("(super)");
    }

    @Test
    public void assertSuperOneArgParses() throws TokenizerException, ParseException {
        assertOptionalSuperParses("(super 1)");
    }
    
    public static void assertInitParses(final String string) throws TokenizerException, ParseException {
        new Parser(Tokenizer.tokenize(string)).parseConsDef(0);
    }

    @Test
    public void testConsDefNoParamsOrSuper() throws TokenizerException, ParseException {
        assertInitParses("(init () (progn))");
    }

    @Test
    public void testConsDefNoParamsSuper() throws TokenizerException, ParseException {
        assertInitParses("(init () (super) (progn))");
    }

    public static void assertMethodParses(final String string) throws TokenizerException, ParseException {
        new Parser(Tokenizer.tokenize(string)).parseMethodDef(0);
    }

    @Test
    public void testGetter() throws TokenizerException, ParseException {
        assertMethodParses("(method int getValue () (return (. this value)))");
    }
}
