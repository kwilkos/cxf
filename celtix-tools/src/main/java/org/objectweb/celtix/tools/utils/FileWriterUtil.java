package org.objectweb.celtix.tools.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Logger;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ToolException;

public class FileWriterUtil {
    private static final Logger LOG = LogUtils.getL7dLogger(FileWriterUtil.class);
    private final File target;

    public FileWriterUtil(String targetDir) throws ToolException {
        target = new File(targetDir);
        if (!(target.exists()) || !(target.isDirectory())) {
            Message msg = new Message("DIRECTORY_NOT_EXIST", LOG, target);
            throw new ToolException(msg);
        }
    }

    public Writer getWriter(String packageName, String fileName) throws IOException {
        File dir = buildDir(packageName);
        File fn = new File(dir, fileName);
        if (fn.exists() && !fn.delete()) {
            throw new IOException(fn + ": Can't delete previous version");
        }
        return new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(fn)));
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
        return packageName.replace('.', File.separatorChar);
    }

}
