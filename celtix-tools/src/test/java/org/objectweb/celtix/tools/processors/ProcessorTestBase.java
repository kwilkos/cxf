package org.objectweb.celtix.tools.processors;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import junit.framework.TestCase;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;

public class ProcessorTestBase extends TestCase {
    
    private static final int DELETE_RETRY_SLEEP_MILLIS = 10;
    protected ProcessorEnvironment env = new ProcessorEnvironment();
    protected File output;
        
    public void setUp() throws Exception {
        URL url = ProcessorTestBase.class.getResource(".");
        output = new File(url.getFile());
        output = new File(output, "/resources");
        if (!output.exists()) {
            output.mkdir();
        }       
    }
    
    public void tearDown() {
        removeDir(output);
        output = null;
        env = null;
    }

    private void removeDir(File d) {
        String[] list = d.list();
        if (list == null) {
            list = new String[0];
        }
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            File f = new File(d, s);
            if (f.isDirectory()) {
                removeDir(f);
            } else {
                delete(f);
            }
        }
        delete(d);
    }

    private void delete(File f) {
        if (!f.delete()) {
            if (isWindows()) {
                System.gc();
            }
            try {
                Thread.sleep(DELETE_RETRY_SLEEP_MILLIS);
            } catch (InterruptedException ex) {
                // Ignore Exception
            }
            if (!f.delete()) {
                f.deleteOnExit();
            }
        }
    }

    private boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.US);
        return osName.indexOf("windows") > -1;
    }
    
    protected String getClassPath() {
        String jarFileDirectory = getClass().getClassLoader().getResource(".").getFile() + "../lib/";

        File file = new File(jarFileDirectory);
        String[] files = file.list();
        String classPath = "";
        for (String str : files) {
            classPath = classPath + jarFileDirectory + File.separatorChar + str
                        + System.getProperty("path.separator");
        }
        return classPath;
    }
      
}
