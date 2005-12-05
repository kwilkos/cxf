package org.objectweb.celtix.tools.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FileWriterUtil {

    private final File target;
    
    public FileWriterUtil(String targetDir) throws IOException {
        target = new File(targetDir);
        if (!(target.exists()) || !(target.isDirectory())) {
            throw new IOException(target + ": Non-existent Directory");
        }
    }
    
    public Writer getWriter(String packageName, String fileName) throws IOException {
        return new FileWriter(getFile(packageName , fileName));

    }
    
    public File getFile(String packageName, String fileName) throws IOException {
        File dir;
        if (packageName == null) {
            dir = target;
        } else {
            dir = new File(target, toDir(packageName));
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File fn = new File(dir , fileName);
        
        if (fn.exists() && !fn.delete()) {      
            throw new IOException(fn + ": Can't delete previous version");          
        }
        return fn;
    }

    
    private String toDir(String packageName) {
        return packageName.replace('.' , File.separatorChar);
    }

}
