package org.objectweb.celtix.tools.util;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

public class FileWriterUtilTest extends TestCase {

    private void cleanDir(File dir) {
        try {
            for (File fl : dir.listFiles()) {
                if (fl.isDirectory()) {
                    cleanDir(fl);
                } else {
                    fl.delete();
                }
            }
        } catch (Exception ex) {
            //ignore
        }
        dir.delete();
    }

    public void testGetFile() throws Exception {
        FileWriterUtil fileWriter = null;    
        String tmpDir = System.getProperty("java.io.tmpdir");
        File targetDir = new File(tmpDir + File.separator + "target");
        try {
            targetDir.mkdirs();
            fileWriter = new FileWriterUtil(targetDir.getAbsolutePath());
            fileWriter.getWriter("com.iona.test" , "A.java");
            String packPath = "/com/iona/test/A.java".replace('/' , File.separatorChar);
            String path = targetDir.getAbsolutePath() + packPath;
            assertNotNull(new File(path).getName());           
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            cleanDir(targetDir);
        }

    }

    public void testGetWriter() throws Exception {
        FileWriterUtil fileWriter = null;
        String tmpDir = System.getProperty("java.io.tmpdir");
        File targetDir = new File(tmpDir + File.separator + "target");

        try {
            targetDir.mkdirs();
            fileWriter = new FileWriterUtil(targetDir.getAbsolutePath()); 
            assertNotNull(fileWriter.getWriter("com.iona.test.SAMPLE" , "A.java"));  
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            cleanDir(targetDir);
        }
    }


}
