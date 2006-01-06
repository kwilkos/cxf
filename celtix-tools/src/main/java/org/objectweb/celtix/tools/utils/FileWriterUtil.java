package org.objectweb.celtix.tools.utils;

import java.io.*;
import org.objectweb.celtix.tools.common.toolspec.ToolException;

public class FileWriterUtil {

    private final File target;
    
    public FileWriterUtil(String targetDir) throws ToolException {
        target = new File(targetDir);
        if (!(target.exists()) || !(target.isDirectory())) {
            throw new ToolException(target + ": Non-existent Directory");
        }
    }
    
    public Writer getWriter(String packageName, String fileName) throws IOException {
        File dir = buildDir(packageName);
        File fn = new File(dir , fileName);
        if (fn.exists() && !fn.delete()) {      
            throw new IOException(fn + ": Can't delete previous version");          
        }
        return new FileWriter(fn);
    }

    public boolean isCollision(String packageName, String fileName) throws ToolException {
        File dir = buildDir(packageName);
        return fileExist(dir, fileName);
    }

    private File buildDir(String packageName) {
        File dir;
        if (packageName == null) {
            dir = target;
        } else {
            dir = new File(target, toDir(packageName));
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    
    private boolean fileExist(File dir, String fileName) {
        return new File(dir, fileName).exists();
    }
    
    private String toDir(String packageName) {
        return packageName.replace('.' , File.separatorChar);
    }

}
