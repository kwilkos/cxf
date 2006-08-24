package org.apache.cxf.tools.common.toolspec.parser;

import junit.framework.TestCase;

import org.apache.cxf.tools.common.toolspec.ToolSpec;

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
        ToolSpec toolspec = new ToolSpec(getClass().getResourceAsStream(tsSource), true);

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

    public void testValidArgumentEnumValue() throws Exception {
        String[] args = new String[] {"-r", "-e", "true", "arg1"};        
        CommandDocument result = parser.parseArguments(args);
        assertEquals("testValidArguments Failed", "true", result.getParameter("enum"));
    }

    public void testInvalidArgumentEnumValue() throws Exception {
        try {
            String[] args = new String[] {"-e", "wrongvalue"};
            parser.parseArguments(args);
            fail("testInvalidArgumentEnumValue failed");
        } catch (BadUsageException ex) {
            Object[] errors = ex.getErrors().toArray();
            assertEquals("testInvalidArgumentEnumValu failed", 1, errors.length);
            CommandLineError error = (CommandLineError)errors[0];
            assertTrue("Expected InvalidArgumentEnumValu error", error instanceof ErrorVisitor.UserError);
            ErrorVisitor.UserError userError = (ErrorVisitor.UserError)error;
            assertEquals("Invalid enum argument value message incorrect", 
                         "-e wrongvalue not in the enumeration value list!", 
                         userError.toString());            
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

    
    public void testInvalidPackageName() {

        try {
            String[] args = new String[]{
                "-p", "/test", "arg1"
            };
            parser.parseArguments(args);
            fail("testInvalidPackageName failed");
        } catch (BadUsageException ex) {
            Object[] errors = ex.getErrors().toArray();
            assertEquals("testInvalidPackageName failed", 1, errors.length);
            CommandLineError error = (CommandLineError)errors[0];
            assertTrue("Expected InvalidArgumentValue error", error instanceof ErrorVisitor.UserError);
            ErrorVisitor.UserError userError = (ErrorVisitor.UserError)error;
            assertEquals("Invalid argument value message incorrect",
                    "-p has invalid character!", userError.toString());
        }

    }

    public void testvalidPackageName() throws Exception {

        String[] args = new String[]{
            "-p", "http://www.iona.com/hello_world_soap_http=com.iona", "-r", "arg1"
        };
        CommandDocument result = parser.parseArguments(args);
        assertEquals("testValidPackageName Failed",
                     "http://www.iona.com/hello_world_soap_http=com.iona",
                     result.getParameter("packagename"));

    }
    
    
    public void testUsage() throws Exception {
        String usage =
            "[ -n <C++ Namespace> ] [ -impl ] [ -e <Enum Value> ] -r "
            + "[ -p <[wsdl namespace =]Package Name> ]* [ -? ] [ -v ] <wsdlurl> ";
        String pUsage = parser.getUsage();
        assertEquals("testUsage failed", usage, pUsage);
    }

    public void testDetailedUsage() throws Exception {
        String lineSeparator = System.getProperty("line.separator");
        String usage = "[ -n <C++ Namespace> ]" + lineSeparator;
        usage += "Namespace" + lineSeparator + lineSeparator;
        usage += "[ -impl ]" + lineSeparator;
        usage += "impl" + lineSeparator + lineSeparator;
        usage += "[ -e <Enum Value> ]" + lineSeparator;
        usage += "enum" + lineSeparator + lineSeparator;
        usage += "-r" + lineSeparator;
        usage += "required" + lineSeparator + lineSeparator;
        usage += "[ -p <[wsdl namespace =]Package Name> ]*" + lineSeparator;
        usage += "The java package name to use for the generated code."
            + "Also, optionally specify the wsdl namespace mapping to a particular java packagename."
            + lineSeparator + lineSeparator;
        usage += "[ -? ]" + lineSeparator;
        usage += "help" + lineSeparator + lineSeparator;
        usage += "[ -v ]" + lineSeparator;
        usage += "version" + lineSeparator + lineSeparator;
        usage += "<wsdlurl>" + lineSeparator;
        usage += "WSDL/SCHEMA URL" + lineSeparator + lineSeparator;        
        assertEquals("testUsage failed", 
                     usage.replaceAll(lineSeparator, "").replace(" ", ""), 
                     parser.getDetailedUsage().replaceAll(lineSeparator, "")
                     .replaceAll(" ", "").replaceAll("\\\t", ""));
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
        assertTrue("Namespace".equals(parser.getDetailedUsage("namespace")));
    }

}
