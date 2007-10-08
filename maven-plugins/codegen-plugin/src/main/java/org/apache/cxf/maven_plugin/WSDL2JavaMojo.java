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

package org.apache.cxf.maven_plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.tools.wsdlto.WSDLToJava;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.ExitException;
import org.apache.tools.ant.util.optional.NoExitSecurityManager;
/**
 * @goal wsdl2java
 * @description CXF WSDL To Java Tool
 * @requiresDependencyResolution test
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

        List<URL> urlList = new ArrayList<URL>();
        StringBuffer buf = new StringBuffer();

        try {
            urlList.add(classesDir.toURL());
        } catch (MalformedURLException e) {
            //ignore
        }

        buf.append(classesDir.getAbsolutePath());
        buf.append(File.pathSeparatorChar);

        for (Artifact a : CastUtils.cast(project.getTestArtifacts(), Artifact.class)) {
            try {
                if (a.getFile() != null
                    && a.getFile().exists()) {
                    urlList.add(a.getFile().toURL());
                    buf.append(a.getFile().getAbsolutePath());
                    buf.append(File.pathSeparatorChar);
                    //System.out.println("     " + a.getFile().getAbsolutePath());
                }
            } catch (MalformedURLException e) {
                //ignore
            }
        }
        
        ClassLoader origContext = Thread.currentThread().getContextClassLoader();
        URLClassLoader loader = new URLClassLoader(urlList.toArray(new URL[urlList.size()]),
                                                   origContext);
        String newCp = buf.toString();

        String cp = System.getProperty("java.class.path");
        SecurityManager oldSm = System.getSecurityManager();
        long timestamp = CodegenUtils.getCodegenTimestamp();
        boolean result = true;
        
        try {
            Thread.currentThread().setContextClassLoader(loader);
            System.setProperty("java.class.path", newCp);
            System.setSecurityManager(new NoExitSecurityManager());
            for (int x = 0; x < wsdlOptions.length; x++) {
                processWsdl(wsdlOptions[x], outputDirFile, timestamp);

                File dirs[] = wsdlOptions[x].getDeleteDirs();
                if (dirs != null) {
                    for (int idx = 0; idx < dirs.length; ++idx) {
                        result = result && deleteDir(dirs[idx]);
                    }
                }
            }
        } finally {
            //cleanup as much as we can.
            Bus bus = BusFactory.getDefaultBus(false);
            if (bus != null) {
                bus.shutdown(true);
            }
            Thread.currentThread().setContextClassLoader(origContext);
            System.setSecurityManager(oldSm);
            System.setProperty("java.class.path", cp);
        }
        if (project != null && sourceRoot != null) {
            project.addCompileSourceRoot(sourceRoot);
        }
        if (project != null && testSourceRoot != null) {
            project.addTestCompileSourceRoot(testSourceRoot);
        }

        System.gc();
    }

    private void processWsdl(WsdlOption wsdlOption,
                             File outputDirFile,
                             long cgtimestamp) throws MojoExecutionException {
        File file = new File(wsdlOption.getWsdl());
        // If URL to WSDL, replace ? and & since they're invalid chars for file names
        File doneFile =
            new File(outputDirFile, "." + file.getName().replace('?', '_').replace('&', '_') + ".DONE");
        boolean doWork = cgtimestamp > doneFile.lastModified();
        if (!doneFile.exists()) {
            doWork = true;
        } else if (file.lastModified() > doneFile.lastModified()) {
            doWork = true;
        } else if (isDefServiceName(wsdlOption)) {
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
            doneFile.delete();

            List<String> list = new ArrayList<String>();
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
                String exitOnFinish = System.getProperty("exitOnFinish", "");

                try {
                    StringBuffer strBuffer = new StringBuffer();
                    for (int i = 0; i < list.size(); i++) {
                        strBuffer.append(list.get(i));
                        strBuffer.append(" ");
                    }
                    System.setProperty("exitOnFinish", "YES");
                    WSDLToJava.main((String[])list.toArray(new String[list.size()]));
                    doneFile.createNewFile();
                } catch (ExitException e) {
                    if (e.getStatus() == 0) {
                        doneFile.createNewFile();
                    } else {
                        throw e;
                    }
                } finally {
                    System.setProperty("exitOnFinish", exitOnFinish);
                }
            } catch (Throwable e) {
                getLog().debug(e);
                throw new MojoExecutionException(e.getMessage(), e);
            }
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

    private boolean isDefServiceName(WsdlOption wsdlOption) {
        List args = wsdlOption.extraargs;
        if (args == null) {
            return false;
        }
        for (int i = 0; i < args.size(); i++) {
            if ("-sn".equalsIgnoreCase((String)args.get(i))) {
                return true;
            }
        }
        return false;

    }

}
