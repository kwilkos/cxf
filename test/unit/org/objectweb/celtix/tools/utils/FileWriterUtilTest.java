package org.objectweb.celtix.tools.utils;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
public class FileWriterUtilTest extends TestCase {
    
    public void testGetFile() {
        FileWriterUtil fileWriter = null;    
        try {
            String tmpDir = System.getProperty("java.io.tmpdir");
            File targetDir = new File(tmpDir + File.separator + "target");
            targetDir.mkdirs();
            fileWriter = new FileWriterUtil(targetDir.getAbsolutePath());
            fileWriter.getFile("com.iona.test" , "A.java");
            String packPath = "/com/iona/test/A.java".replace('/' , File.separatorChar);
            String path = targetDir.getAbsolutePath() + packPath;
            assertNotNull(new File(path).getName());           
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         
    }
    
    public void testGetWriter() {
        FileWriterUtil fileWriter = null;
        
        try {
            String tmpDir = System.getProperty("java.io.tmpdir");
            File targetDir = new File(tmpDir + File.separator + "target");
            targetDir.mkdirs();
            fileWriter = new FileWriterUtil(targetDir.getAbsolutePath()); 
            assertNotNull(fileWriter.getWriter("com.iona.test.SAMPLE" , "A.java"));  
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    

}
