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

package org.apache.cxf.maven_plugin.eclipse;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.tools.wsdl2java.frontend.jaxws.VelocityWriter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * @goal eclipseplugin
 * @description CXF eclipse plugin generator
 */
public class EclipsePluginMojo extends AbstractMojo {
    private static final String LIB_PATH = "lib";
    private static final String ECLIPSE_PLUGIN_TEMPLATE = 
        "/org/apache/cxf/maven_plugin/eclipse/3.0/plugin.xml.vm";
    /**
     * @parameter expression="${project}"
     * @required
     */
    MavenProject project;

    /**
     * The set of dependencies required by the project 
     * @parameter default-value="${project.artifacts}"
     * @required
     * @readonly
     */
    java.util.Set dependencies;

    /**
     * @parameter  expression="${project.build.directory}/plugin.xml";
     * @required
     */
    File targetFile;



    private List listJars() throws Exception {
        List jars = new ArrayList();
        if (dependencies != null && !dependencies.isEmpty()) {            
            for (Iterator it = dependencies.iterator(); it.hasNext();) {
                Artifact artifact = (Artifact)it.next();
                File oldJar = artifact.getFile();
                jars.add(oldJar);
            }            
        }
        return jars;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        
        try {
            generatePluginXML(listJars());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }


    private String getVelocityLogFile(String log) {
        return new File(targetFile.getParentFile(), log).toString();
    }

    private String getVersion() {
        return StringUtils.formatVersionNumber(project.getVersion());
    }

    // TODO: Reuse the velocity in the tools 
    private void initVelocity() throws Exception {
        Properties props = new Properties();
        String clzName = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
        props.put("resource.loader", "class");
        props.put("class.resource.loader.class", clzName);
        props.put("runtime.log", getVelocityLogFile("velocity.log"));

        Velocity.init(props);

    }

    private void generatePluginXML(List jars) throws Exception {
        initVelocity();

        Template tmpl = null;

        tmpl = Velocity.getTemplate(ECLIPSE_PLUGIN_TEMPLATE);

        if (tmpl == null) {
            throw new RuntimeException("Can not load template file: " + ECLIPSE_PLUGIN_TEMPLATE);
        }

        VelocityContext ctx = new VelocityContext();
        ctx.put("ECLIPSE_VERSION", "3.0");
        ctx.put("PLUGIN_VERSION", getVersion());
        ctx.put("GROUP_ID", project.getGroupId());
        ctx.put("libPath", LIB_PATH);
        ctx.put("jars", jars);

        Writer outputs = null;

        outputs = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(targetFile)), "UTF-8");
        VelocityWriter writer = new VelocityWriter(outputs);

        tmpl.merge(ctx, writer);
        writer.close();
    }
}
