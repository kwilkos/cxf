package org.objectweb.celtix.routing;

import java.io.File;
import java.util.List;

import javax.xml.namespace.QName;
import org.objectweb.celtix.routing.configuration.DestinationType;
import org.objectweb.celtix.routing.configuration.RouteType;
import org.objectweb.celtix.routing.configuration.SourceType;
import org.objectweb.celtix.testutil.common.TestUtil;
import org.objectweb.celtix.tools.WSDLToJava;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolRunner;

public final class RouteTypeUtil {
    private RouteTypeUtil() {
        //Complete
    }

    public static RouteType createRouteType(String routeName,
                                      QName srcService, String srcPort,
                                      QName destService, String destPort) {
        SourceType st = new SourceType();
        st.setService(srcService);
        st.setPort(srcPort);

        DestinationType dt = new DestinationType();
        dt.setPort(destPort);
        dt.setService(destService);

        RouteType rt = new RouteType();
        rt.setName(routeName);

        List<SourceType> sList = rt.getSource();
        sList.add(st);
        List<DestinationType> dList = rt.getDestination();
        dList.add(dt);

        return rt;
    }
  
    //Provide a valid srcDir and a classDir for wsdltojava to generate .java and .class
    // files for the specified wsdlUrl.
    public static void invokeWSDLToJava(String wsdlUrl, File srcDir, File classDir) throws Exception {
        String[] args = new String[]{"-compile", 
                                     "-d", srcDir.getCanonicalPath(),
                                     "-classdir", classDir.getCanonicalPath(),
                                     wsdlUrl};

        ToolRunner.runTool(WSDLToJava.class,
                           WSDLToJava.class.getResourceAsStream(ToolConstants.TOOLSPECS_BASE
                                                                + "wsdl2java.xml"),
                           false,
                           args);        
    }
    
    public static boolean deleteDir(File dir) {
        return TestUtil.deleteDir(dir);
    }
    
    public static String getClassPath(ClassLoader loader) {
        return TestUtil.getClassPath(loader);
    }
}
