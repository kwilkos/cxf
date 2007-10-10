/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.helpers;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public final class FileUtils {
    private static final int RETRY_SLEEP_MILLIS = 10;
    private static File defaultTempDir;
    
    
    private FileUtils() {
        
    }
    
    private static synchronized File getDefaultTempDir() {
        if (defaultTempDir != null) {
            return defaultTempDir;
        }
        String s = System.getProperty(FileUtils.class.getName() + ".TempDirectory");
        if (s == null) {
            int x = (int)(Math.random() * 1000000);
            s = System.getProperty("java.io.tmpdir");
            File f = new File(s, "cxf-tmp-" + x);
            while (!f.mkdir()) {
                x = (int)(Math.random() * 1000000);
                f = new File(s, "cxf-tmp-" + x);
            }
            defaultTempDir = f;
            Thread hook = new Thread() {
                @Override
                public void run() {
                    removeDir(defaultTempDir);
                }
            };
            Runtime.getRuntime().addShutdownHook(hook);            
        } else {
            //assume someone outside of us will manage the directory
            File f = new File(s);
            f.mkdirs();
            defaultTempDir = f;
        }
        return defaultTempDir;
    }

    public static void mkDir(File dir) {
        if (dir == null) {
            throw new RuntimeException("dir attribute is required");
        }

        if (dir.isFile()) {
            throw new RuntimeException("Unable to create directory as a file "
                                    + "already exists with that name: " + dir.getAbsolutePath());
        }

        if (!dir.exists()) {
            boolean result = doMkDirs(dir);
            if (!result) {
                String msg = "Directory " + dir.getAbsolutePath()
                             + " creation was not successful for an unknown reason";
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * Attempt to fix possible race condition when creating directories on
     * WinXP, also Windows2000. If the mkdirs does not work, wait a little and
     * try again.
     */
    private static boolean doMkDirs(File f) {
        if (!f.mkdirs()) {
            try {
                Thread.sleep(RETRY_SLEEP_MILLIS);
                return f.mkdirs();
            } catch (InterruptedException ex) {
                return f.mkdirs();
            }
        }
        return true;
    }

    public static void removeDir(File d) {
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

    public static void delete(File f) {
        if (!f.delete()) {
            if (isWindows()) {
                System.gc();
            }
            try {
                Thread.sleep(RETRY_SLEEP_MILLIS);
            } catch (InterruptedException ex) {
                // Ignore Exception
            }
            if (!f.delete()) {
                f.deleteOnExit();
            }
        }
    }

    private static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.US);
        return osName.indexOf("windows") > -1;
    }

    public static File createTempFile(String prefix, String suffix) throws IOException {
        return createTempFile(prefix, suffix, null, false);
    }
    
    public static File createTempFile(String prefix, String suffix, File parentDir,
                               boolean deleteOnExit) throws IOException {
        File result = null;
        File parent = (parentDir == null)
            ? getDefaultTempDir()
            : parentDir;
            
        if (suffix == null) {
            suffix = ".tmp";
        }
        if (prefix == null) {
            prefix = "cxf";
        } else if (prefix.length() < 3) {
            prefix = prefix + "cxf";
        }
        result = File.createTempFile(prefix, suffix, parent);

        //if parentDir is null, we're in our default dir
        //which will get completely wiped on exit from our exit
        //hook.  No need to set deleteOnExit() which leaks memory.
        if (deleteOnExit && parentDir != null) {
            result.deleteOnExit();
        }
        return result;
    }
}
