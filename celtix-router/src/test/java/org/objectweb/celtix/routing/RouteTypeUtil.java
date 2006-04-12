package org.objectweb.celtix.routing;

import java.io.File;
import java.util.List;

import javax.xml.namespace.QName;
import org.objectweb.celtix.routing.configuration.DestinationType;
import org.objectweb.celtix.routing.configuration.RouteType;
import org.objectweb.celtix.routing.configuration.SourceType;

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
    
    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
}
