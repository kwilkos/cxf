package celtixinstaller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public final class Installer {
    static final Set<String> BINARY_EXTS = new TreeSet<String>();
    static {
        BINARY_EXTS.add("jar");
        BINARY_EXTS.add("zip");
        BINARY_EXTS.add("gif");
        BINARY_EXTS.add("jpg");
        BINARY_EXTS.add("jpeg");
        BINARY_EXTS.add("pdf");
        BINARY_EXTS.add("png");
        BINARY_EXTS.add("odt");
        BINARY_EXTS.add("ott");
        BINARY_EXTS.add("xsd");
    }

    private Installer() {
        //never constructed
    }

    private static boolean isBinary(String s) {
        if (s.indexOf("maven_repo") != -1) {
            return true;
        }
        for (String ext : BINARY_EXTS) {
            if (s.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String args[]) throws Exception {
        File outputDir = new File(".");
        outputDir = outputDir.getCanonicalFile();

        if (args.length != 0) {
            outputDir = new File(args[0]);
        }

        System.out.println("Unpacking celtix to " + outputDir.toString());


        URL url = Installer.class.getResource("/celtixinstaller/Installer.class");
        String jarf = url.getFile();
        jarf = jarf.substring(0, jarf.indexOf("!"));
        url = new URL(jarf);

        byte buffer[] = new byte[4096];
        JarInputStream jin = new JarInputStream(new FileInputStream(url.getFile()));
        List<String> executes = new ArrayList<String>();


        for (JarEntry entry = jin.getNextJarEntry(); entry != null; entry = jin.getNextJarEntry()) {
            if (entry.isDirectory()) {
                if (!entry.getName().startsWith("META-INF") 
                    && !entry.getName().startsWith("celtixinstaller")) {
                    File file = new File(outputDir, entry.getName());
                    file.mkdirs();
                    file.setLastModified(entry.getTime());
                }
            } else if (!entry.getName().startsWith("META-INF")
                       && !entry.getName().startsWith("celtixinstaller")) {

                boolean binary = isBinary(entry.getName().toLowerCase());
                if (entry.getName().indexOf("/bin/") != -1
                    || entry.getName().indexOf("\\bin\\") != -1) {
                    executes.add(entry.getName());
                }

                File outFile = new File(outputDir, entry.getName());
                if (binary) {
                    OutputStream out = new FileOutputStream(outFile);
                    for (int len = jin.read(buffer); len != -1; len = jin.read(buffer)) {
                        out.write(buffer, 0, len);
                    }
                    out.close();
                } else {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(jin));
                    for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                        writer.write(s);
                        writer.newLine();
                    }
                    writer.close();
                }
                outFile.setLastModified(entry.getTime());
            }
        }

        if (System.getProperty("os.name").indexOf("Windows") == -1
            && !executes.isEmpty()) {
            //add executable bit
            executes.add(0, "chmod");
            executes.add(1, "+x");

            Runtime.getRuntime().exec(executes.toArray(new String[executes.size()]));
        }
    }
}
