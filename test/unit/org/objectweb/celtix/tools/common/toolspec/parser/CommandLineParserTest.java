package org.objectweb.celtix.tools.common.toolspec.parser;

import junit.framework.TestCase;

import org.objectweb.celtix.tools.common.toolspec.ToolSpec;

public class CommandLineParserTest extends TestCase {
    private CommandLineParser parser;

    public CommandLineParserTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(CommandLineParserTest.class);
    }

    public void setUp() throws Exception {
        String tsSource = "/org/objectweb/celtix/tools/common/toolspec/parser/resources/testtool.xml";
        ToolSpec toolspec = new ToolSpec(getClass().getResourceAsStream(tsSource), false);

        parser = new CommandLineParser(toolspec);
    }

    public void testValidArguments() throws Exception {
        String[] args = new String[] {"-r", "-n", "test", "arg1"};
        CommandDocument result = parser.parseArguments(args);

        assertEquals("testValidArguments Failed", "test", result.getParameter("namespace"));
    }

    public void testInvalidArgumentValue() throws Exception {
        try {
            String[] args = new String[] {"-n", "test@", "arg1"};
            //            CommandDocument result = parser.parseArguments(args);
            parser.parseArguments(args);
            fail("testInvalidArgumentValue failed");
        } catch (BadUsageException ex) {
            Object[] errors = ex.getErrors().toArray();
            assertEquals("testInvalidArgumentValue failed", 1, errors.length);
            CommandLineError error = (CommandLineError)errors[0];
            assertTrue("Expected InvalidArgumentValue error", error instanceof ErrorVisitor.UserError);
            ErrorVisitor.UserError userError = (ErrorVisitor.UserError)error;
            assertEquals("Invalid argument value message incorrect", "-n has invalid character!", userError
                .toString());
        }
    }

    public void testValidMixedArguments() throws Exception {
        String[] args = new String[] {"-v", "-r", "-n", "test", "arg1"};
        CommandDocument result = parser.parseArguments(args);

        assertEquals("testValidMissedArguments Failed", "test", result.getParameter("namespace"));
    }

    public void testInvalidOption() {
        try {
            String[] args = new String[] {"-n", "-r", "arg1"};
            //            CommandDocument result =
            parser.parseArguments(args);

            fail("testInvalidOption failed");
        } catch (BadUsageException ex) {
            Object[] errors = ex.getErrors().toArray();

            assertEquals("testInvalidOption failed", 1, errors.length);
            CommandLineError error = (CommandLineError)errors[0];

            assertTrue("Expected InvalidOption error", error instanceof ErrorVisitor.InvalidOption);
            ErrorVisitor.InvalidOption option = (ErrorVisitor.InvalidOption)error;

            assertEquals("Invalid option incorrect", "-n", option.getOptionSwitch());
            assertEquals("Invalid option message incorrect",
                         "Invalid option: -n is missing its associated argument", option.toString());
        }
    }

    public void testMissingOption() {
        try {
            String[] args = new String[] {"-n", "test", "arg1"};
            //            CommandDocument result = parser.parseArguments(args);
            parser.parseArguments(args);
            fail("testMissingOption failed");
        } catch (BadUsageException ex) {
            Object[] errors = ex.getErrors().toArray();

            assertEquals("testInvalidOption failed", 1, errors.length);
            CommandLineError error = (CommandLineError)errors[0];

            assertTrue("Expected MissingOption error", error instanceof ErrorVisitor.MissingOption);
            ErrorVisitor.MissingOption option = (ErrorVisitor.MissingOption)error;

            assertEquals("Missing option incorrect", "r", option.getOptionSwitch());
        }
    }

    public void testMissingArgument() {
        try {
            String[] args = new String[] {"-n", "test", "-r"};
            //            CommandDocument result = 
            parser.parseArguments(args);
            fail("testMissingArgument failed");
        } catch (BadUsageException ex) {
            Object[] errors = ex.getErrors().toArray();

            assertEquals("testInvalidOption failed", 1, errors.length);
            CommandLineError error = (CommandLineError)errors[0];

            assertTrue("Expected MissingArgument error", error instanceof ErrorVisitor.MissingArgument);
            ErrorVisitor.MissingArgument arg = (ErrorVisitor.MissingArgument)error;

            assertEquals("MissingArgument incorrect", "wsdlurl", arg.getArgument());
        }
    }

    public void testDuplicateArgument() {
        try {
            String[] args = new String[] {"-n", "test", "-r", "arg1", "arg2"};
            //            CommandDocument result = 
            parser.parseArguments(args);
            fail("testUnexpectedArgument failed");
        } catch (BadUsageException ex) {
            Object[] errors = ex.getErrors().toArray();
            assertEquals("testInvalidOption failed", 1, errors.length);
            CommandLineError error = (CommandLineError)errors[0];
            assertTrue("Expected UnexpectedArgument error", error instanceof ErrorVisitor.UnexpectedArgument);
        }
    }

    public void testUnexpectedOption() {
        try {
            String[] args = new String[] {"-n", "test", "-r", "-unknown"};
            //            CommandDocument result = 
            parser.parseArguments(args);
            fail("testUnexpectedOption failed");
        } catch (BadUsageException ex) {
            Object[] errors = ex.getErrors().toArray();

            assertEquals("testInvalidOption failed", 1, errors.length);
            CommandLineError error = (CommandLineError)errors[0];

            assertTrue("Expected UnexpectedOption error", error instanceof ErrorVisitor.UnexpectedOption);
            ErrorVisitor.UnexpectedOption option = (ErrorVisitor.UnexpectedOption)error;

            assertEquals("UnexpectedOption incorrect", "-unknown", option.getOptionSwitch());
        }
    }

    public void testUsage() throws Exception {
        String usage = "[ -n <C++ Namespace> ] [ -impl ] -r [ -? ] [ -v ] <wsdlurl> ";
        String pUsage = parser.getUsage();
        assertEquals("testUsage failed", usage, pUsage);
    }

    public void testDetailedUsage() throws Exception {
        String lineSeparator = System.getProperty("line.separator");
        String usage = "[ -n <C++ Namespace> ]" + lineSeparator;
        usage += "Namespace" + lineSeparator + lineSeparator;
        usage += "[ -impl ]" + lineSeparator;
        usage += "impl" + lineSeparator + lineSeparator;
        usage += "-r" + lineSeparator;
        usage += "required" + lineSeparator + lineSeparator;
        usage += "[ -? ]" + lineSeparator;
        usage += "help" + lineSeparator + lineSeparator;
        usage += "[ -v ]" + lineSeparator;
        usage += "version" + lineSeparator + lineSeparator;
        usage += "<wsdlurl>" + lineSeparator;
        usage += "WSDL/SCHEMA URL" + lineSeparator + lineSeparator;
        assertEquals("testUsage failed", usage, parser.getDetailedUsage());
    }

    public void testOtherMethods() throws Exception {
        String tsSource = "/org/objectweb/celtix/tools/common/toolspec/parser/resources/testtool.xml";
        ToolSpec toolspec = new ToolSpec(getClass().getResourceAsStream(tsSource), false);
        CommandLineParser commandLineParser = new CommandLineParser(null);
        commandLineParser.setToolSpec(toolspec);
        CommandDocument commandDocument = commandLineParser.parseArguments("-r unknown");
        assertTrue(commandDocument != null);
    }

    public void testGetDetailedUsage() {
        assertTrue(parser.getDetailedUsage("namespace").equals("Namespace"));
    }

}
