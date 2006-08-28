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

import org.apache.cxf.resource.ClassLoaderResolver;
import org.apache.cxf.resource.DefaultResourceManager;
import org.apache.cxf.tools.generators.spring.BeanGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.ExitException;
import org.apache.tools.ant.util.optional.NoExitSecurityManager;

/**
 * @goal beangen
 * @description CXF BeanGen Tool
 */
public class BeanGenMojo extends AbstractMojo {
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
     * @parameter  expression="${basedir}/src/main/resources"
     * @required
     */
    String resourcesRoot;    
    
    /**
     * @parameter expression="${project}"
     * @required
     */
    MavenProject project;
    
    /**
     * @parameter  expression="${project.compileClasspathElements}"
     * @required
     */
    List classpathElements;
    
    /**
     * @parameter
     */
    String beanfiles[];

    
    public void execute() throws MojoExecutionException {
        String outputDir = testSourceRoot == null ? sourceRoot : testSourceRoot;
        File outputDirFile = new File(outputDir);
        outputDirFile.mkdirs();
        long timestamp = CodegenUtils.getCodegenTimestamp();
        
        List list = new ArrayList();
        List doneFiles = new ArrayList();
        list.add("-d");
        list.add(outputDir);
        for (int x = 0; x < beanfiles.length; x++) {
            File file = new File(beanfiles[x]);
            File doneFile = new File(outputDirFile, "." + file.getName() + ".DONE");
            if (!doneFile.exists()
                || file.lastModified() > doneFile.lastModified()
                || timestamp > doneFile.lastModified()) {
                list.add(beanfiles[x]);
                doneFiles.add(doneFile);
            }
        }
        
        List urlList = new ArrayList();
        Iterator it = classpathElements.iterator();
        File file = new File(resourcesRoot);
        try {
            urlList.add(file.toURL());
        } catch (MalformedURLException e) {
            //ignore
        }
        while (it.hasNext()) {
            String el = (String)it.next();
            file = new File(el);
            try {
                urlList.add(file.toURL());
            } catch (MalformedURLException e) {
                //ignore
            }
        }
        
        URLClassLoader loader = new URLClassLoader((URL[])urlList.toArray(new URL[urlList.size()]),
                                                   this.getClass().getClassLoader());
        
        if (list.size() > 2) {
            SecurityManager oldSm = System.getSecurityManager();
            try {
                try {
                    DefaultResourceManager.instance().addResourceResolver(new ClassLoaderResolver(loader));
                    System.setSecurityManager(new NoExitSecurityManager());
                    
                    BeanGenerator.main((String[])list.toArray(new String[list.size()]));
                    throw new ExitException(0);
                } catch (ExitException e) {
                    if (e.getStatus() == 0) {
                        it = doneFiles.iterator();
                        while (it.hasNext()) {
                            File doneFile = (File)it.next();
                            doneFile.delete();
                            doneFile.createNewFile();
                        }
                    } else {
                        throw e;
                    }
                } finally {
                    System.setSecurityManager(oldSm);
                    DefaultResourceManager.clearInstance();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                throw new MojoExecutionException(e.getMessage(), e);
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
