package org.apache.cxf.tools.java2wsdl;

import org.apache.cxf.tools.common.ToolTestBase;

public class JavaToWSDLTest extends ToolTestBase {

    public void testVersionOutput() throws Exception {
        String[] args = new String[] {"-v"};
        JavaToWSDL.main(args);
        assertNotNull(getStdOut());
    }

    public void testHelpOutput() {
        String[] args = new String[] {"-help"};
        JavaToWSDL.main(args);
        assertNotNull(getStdOut());
    }

    public void testNormalArgs() {
        String[] args = new String[] {"-o", "./test.wsdl", "org.apache.hello_world_soap_http.Greeter"};
        JavaToWSDL.main(args);
        assertNotNull(getStdOut());
    }

    public void testBadUsage() {
        String[] args = new String[] {"-ttt", "a.ww"};
        JavaToWSDL.main(args);
        assertNotNull(getStdOut());

    }

    public void testValidArgs() {
        String[] args = new String[] {"a.ww"};
        JavaToWSDL.main(args);
        assertNotNull(getStdOut());

    }

    public void testNoOutPutFile() {
        String[] args = new String[] {"org.apache.hello_world_soap_http.Greeter"};
        JavaToWSDL.main(args);
        assertNotNull(getStdOut());
    }

}
