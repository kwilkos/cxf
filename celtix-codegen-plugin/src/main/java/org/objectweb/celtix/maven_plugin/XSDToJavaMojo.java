package org.objectweb.celtix.maven_plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.ExitException;
import org.apache.tools.ant.util.optional.NoExitSecurityManager;

/**
 * @goal xsdtojava
 * @description Celtix XSD To Java Tool
 */
public class XSDToJavaMojo extends AbstractMojo {
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
     * @parameter expression="${project}"
     * @required
     */
    MavenProject project;
    
    
    /**
     * @parameter
     */
    XSDOption xsdOptions[];

    
    public void execute() throws MojoExecutionException {
        String outputDir = testSourceRoot == null ? sourceRoot : testSourceRoot;
        File outputDirFile = new File(outputDir);
        outputDirFile.mkdirs();
        
        long timestamp = CodegenUtils.getCodegenTimestamp();
        boolean result = true;
        
        if (xsdOptions == null) {
            throw new MojoExecutionException("Must specify xsdOptions");           
        }
        for (int x = 0; x < xsdOptions.length; x++) {
            List list = new ArrayList();
            if (xsdOptions[x].getPackagename() != null) {
                list.add("-p");
                list.add(xsdOptions[x].getPackagename());
            }
            if (xsdOptions[x].getBindingFile() != null) {
                list.add("-b");
                list.add(xsdOptions[x].getBindingFile());
            }
            list.add("-quiet");
            list.add("-d");
            list.add(outputDir);
            list.add(xsdOptions[x].getXsd());
            
            File file = new File(xsdOptions[x].getXsd());
            File doneFile = new File(outputDirFile, "." + file.getName() + ".DONE");
            boolean doWork = timestamp > doneFile.lastModified();
            if (!doneFile.exists()) {
                doWork = true;
            } else if (file.lastModified() > doneFile.lastModified()) {
                doWork = true;
            } else {
                File files[] = xsdOptions[x].getDependencies();
                if (files != null) {
                    for (int z = 0; z < files.length; ++z) {
                        if (files[z].lastModified() > doneFile.lastModified()) {
                            doWork = true;
                        }
                    }
                }
            }
            
            if (doWork) {
                SecurityManager oldSm = System.getSecurityManager();
                try {
                    try {
                        System.setSecurityManager(new NoExitSecurityManager());
                        
                        com.sun.tools.xjc.Driver.main((String[])list.toArray(new String[list.size()]));
                       
                    } catch (ExitException e) {
                        if (e.getStatus() == 0) {
                            doneFile.delete();
                            doneFile.createNewFile();
                        } else {
                            throw e;
                        }
                    } finally {
                        System.setSecurityManager(oldSm);
                        File dirs[] = xsdOptions[x].getDeleteDirs();
                        if (dirs != null) {
                            for (int idx = 0; idx < dirs.length; ++idx) {
                                result = result && deleteDir(dirs[idx]);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        
            if (!result) {
                throw new MojoExecutionException("Could not delete redundant dirs");
            }                
        }
        
        if (project != null && sourceRoot != null) {
            project.addCompileSourceRoot(sourceRoot);
        }
        if (project != null && testSourceRoot != null) {
            project.addTestCompileSourceRoot(testSourceRoot);
        }
    }
    
    private boolean deleteDir(File f) {
        if (f.isDirectory()) {
            File files[] = f.listFiles();
            for (int idx = 0; idx < files.length; ++idx) {
                deleteDir(files[idx]);
            }
        }
        
        if (f.exists()) {
            return f.delete();
        }
        
        return true;
    }
}
