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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.cxf.helpers.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

/**
   This class was ported from fAnt wsdl2java task
   http://code.google.com/p/fant/
 */

public final class WsdlOptionLoader {
    private static final String WSDL_SUFFIX = ".+\\.wsdl$";
    private static final String WSDL_OPTIONS = "-options$";
    private static final String WSDL_BINDINGS = "-binding-?\\d*.xml$";

    public List<WsdlOption> load(String wsdlRoot) throws MojoExecutionException {
        return load(new File(wsdlRoot));
    }

    public List<WsdlOption> load(File wsdlBasedir) throws MojoExecutionException {
        if (wsdlBasedir == null) {
            return new ArrayList<WsdlOption>();
        }

        if (!wsdlBasedir.exists()) {
            throw new MojoExecutionException(wsdlBasedir + " not exists");
        }

        return findJobs(wsdlBasedir, getWsdlFiles(wsdlBasedir));
    }

    private List<File> getWsdlFiles(File dir) {
        return FileUtils.getFiles(dir, WSDL_SUFFIX);
    }

    private File getOptions(File dir, String pattern) {
        List<File> files = FileUtils.getFiles(dir, pattern);
        if (files.size() > 0) {
            return files.iterator().next();
        }
        return null;
    }

    private List<File> getBindingFiles(File dir, String pattern) {
        return FileUtils.getFiles(dir, pattern);
    }

    protected List<WsdlOption> findJobs(File dir, List<File> wsdlFiles) {
        List<WsdlOption> jobs = new ArrayList<WsdlOption>();

        for (File wsdl : wsdlFiles) {
            if (wsdl == null || !wsdl.exists()) {
                continue;
            }

            String wsdlName = wsdl.getName();
            wsdlName = wsdlName.substring(0, wsdlName.indexOf(".wsdl"));
            File options = getOptions(dir, wsdlName + WSDL_OPTIONS);
            List<File> bindings = getBindingFiles(dir, wsdlName + WSDL_BINDINGS);

            jobs.add(generateWsdlOption(wsdl, bindings, options));
        }
        return jobs;
    }

    protected WsdlOption generateWsdlOption(final File wsdl, 
                                            final List<File> bindingFiles, 
                                            final File options) {
        WsdlOption wsdlOption = new WsdlOption();

        if (bindingFiles != null) {
            for (File binding : bindingFiles) {
                wsdlOption.getExtraargs().add("-b");
                wsdlOption.getExtraargs().add(binding.toString());
            }
        }

        if (options != null && options.exists()) {
            try {
                List<String> lines = FileUtils.readLines(options);
                if (lines.size() > 0) {
                    wsdlOption.getExtraargs().addAll(Arrays.asList(lines.iterator().next().split(" ")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        wsdlOption.setWsdl(wsdl.toString());
        
        return wsdlOption;
    }
}