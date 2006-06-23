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
import org.objectweb.celtix.tools.wsdl2java.WSDLToJava;
/**
 * @goal wsdl2java
 * @description Celtix WSDL To Java Tool
 */
public class WSDL2JavaMojo extends AbstractMojo {
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

        
        if (wsdlOptions == null) {
            throw new MojoExecutionException("Must specify wsdlOptions");           
        }

        StringBuffer buf = new StringBuffer();
        Iterator it = classpathElements.iterator();
        while (it.hasNext()) {
            buf.append(it.next().toString());
            buf.append(File.pathSeparatorChar);
        }
        String newCp = buf.toString();
        
        String cp = System.getProperty("java.class.path");
        SecurityManager oldSm = System.getSecurityManager();
        long timestamp = CodegenUtils.getCodegenTimestamp();
        try {
            System.setProperty("java.class.path", newCp);
            System.setSecurityManager(new NoExitSecurityManager());
        
            for (int x = 0; x < wsdlOptions.length; x++) {
                processWsdl(wsdlOptions[x], outputDirFile, timestamp);
            }
        } finally {
            System.setSecurityManager(oldSm);
            System.setProperty("java.class.path", cp);
        }        
        if (project != null && sourceRoot != null) {
            project.addCompileSourceRoot(sourceRoot);
        }
        if (project != null && testSourceRoot != null) {
            project.addTestCompileSourceRoot(testSourceRoot);
        }        
    }
    
    private void processWsdl(WsdlOption wsdlOption,
                             File outputDirFile,
                             long cgtimestamp) throws MojoExecutionException {
        File file = new File(wsdlOption.getWsdl());
        File doneFile = new File(outputDirFile, "." + file.getName() + ".DONE");
        boolean doWork = cgtimestamp > doneFile.lastModified();
        if (!doneFile.exists()) {
            doWork = true;
        } else if (file.lastModified() > doneFile.lastModified()) {
            doWork = true;
        } else {
            File files[] = wsdlOption.getDependencies();
            if (files != null) {
                for (int z = 0; z < files.length; ++z) {
                    if (files[z].lastModified() > doneFile.lastModified()) {
                        doWork = true;
                    }
                }
            }
        }            
        
        if (doWork) {
            
            List list = new ArrayList();
            if (wsdlOption.getPackagenames() != null) {
                Iterator it = wsdlOption.getPackagenames().iterator();
                while (it.hasNext()) {
                    list.add("-p");
                    list.add(it.next().toString());
                }
            }
            // -d specify the dir for generated source code
            //list.add("-verbose");
            list.add("-d");
            list.add(outputDirFile.toString());
            
            if (wsdlOption.getExtraargs() != null) {
                Iterator it = wsdlOption.getExtraargs().iterator();
                while (it.hasNext()) {
                    list.add(it.next().toString());
                }
            }            
            list.add(wsdlOption.getWsdl());            
            
            
            try {
                try {
                    WSDLToJava.main((String[])list.toArray(new String[list.size()]));
                    doneFile.delete();
                    doneFile.createNewFile();
                } catch (ExitException e) {
                    if (e.getStatus() == 0) {
                        doneFile.delete();
                        doneFile.createNewFile();
                    } else {
                        throw e;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }
}
