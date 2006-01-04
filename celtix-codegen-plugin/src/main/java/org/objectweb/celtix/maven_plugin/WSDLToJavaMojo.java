package org.objectweb.celtix.maven_plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.ExitException;
import org.apache.tools.ant.util.optional.NoExitSecurityManager;

/**
 * @goal wsdltojava
 * @description Celtix WSDL To Java Tool
 */
public class WSDLToJavaMojo extends AbstractMojo {
    /**
     * @parameter
     */
    String testSourceRoot;
    
    /**
     * @parameter  expression="${project.build.directory}/generated/src/main/java"
     * @required
     */
    String sourceRoot;
    
    /**
     * @parameter  expression="${project.build.outputDirectory}"
     * @required
     */
    String classesDirectory;
    
    /**
     * @parameter  expression="${project.compileClasspathElements}"
     * @required
     */
    List classpathElements;

    /**
     * @parameter expression="${project}"
     * @required
     */
    MavenProject project;
    
    
    /**
     * @parameter
     */
    WsdlOption wsdlOptions[];

    
    public void execute() throws MojoExecutionException {
        String outputDir = testSourceRoot == null ? sourceRoot : testSourceRoot;
        File outputDirFile = new File(outputDir);
        outputDirFile.mkdirs();
        
        File classesDir = new File(classesDirectory);
        classesDir.mkdirs();
        
        StringBuffer buf = new StringBuffer();
        Iterator it = classpathElements.iterator();
        while (it.hasNext()) {
            buf.append(it.next().toString());
            buf.append(File.pathSeparatorChar);
        }
        String newCp = buf.toString();
        
        for (int x = 0; x < wsdlOptions.length; x++) {
            List list = new ArrayList();
            if (wsdlOptions[x].getPackagenames() != null) {
                it = wsdlOptions[x].getPackagenames().iterator();
                while (it.hasNext()) {
                    list.add("-p");
                    list.add(it.next().toString());
                }
            }
            list.add("-keep");
            list.add("-d");
            list.add(classesDirectory);
            list.add("-s");
            list.add(outputDir);
            
            if (wsdlOptions[x].getExtraargs() != null) {
                it = wsdlOptions[x].getExtraargs().iterator();
                while (it.hasNext()) {
                    list.add(it.next().toString());
                }
            }            
            list.add(wsdlOptions[x].getWsdl());
            
            File file = new File(wsdlOptions[x].getWsdl());
            File doneFile = new File(outputDirFile, "." + file.getName() + ".DONE");
            if (!doneFile.exists()
                || file.lastModified() > doneFile.lastModified()) {
            
                String cp = System.getProperty("java.class.path");
                SecurityManager oldSm = System.getSecurityManager();
                try {
                    try {
                        System.setProperty("java.class.path", newCp);
                        System.setSecurityManager(new NoExitSecurityManager());
                        
                        com.sun.tools.ws.WsImport.main((String[])list.toArray(new String[list.size()]));
                       
                    } catch (ExitException e) {
                        if (e.getStatus() == 0) {
                            doneFile.delete();
                            doneFile.createNewFile();
                        } else {
                            throw e;
                        }
                    } finally {
                        System.setSecurityManager(oldSm);
                        System.setProperty("java.class.path", cp);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        }
        
        if (project != null && sourceRoot != null) {
            project.addCompileSourceRoot(sourceRoot);
        }
        if (project != null && testSourceRoot != null) {
            project.addTestCompileSourceRoot(testSourceRoot);
        }        
    }
}