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
import java.util.List;

public class WsdlOption {
    String wsdl;
    List packagenames;
    List extraargs;
    File dependencies[];
    File redundantDirs[];

    public List getExtraargs() {
        return extraargs;
    }

    public void setExtraargs(List ea) {
        this.extraargs = ea;
    }

    public List getPackagenames() {
        return packagenames;
    }

    public void setPackagenames(List pn) {
        this.packagenames = pn;
    }

    public String getWsdl() {
        return wsdl;
    }

    public void setWsdl(String w) {
        wsdl = w;
    }
    public void setDependencies(File files[]) {
        dependencies = files;
    }
    public File[] getDependencies() {
        return dependencies;
    }    
    public void setDeleteDirs(File files[]) {
        redundantDirs = files;
    }
    public File[] getDeleteDirs() {
        return redundantDirs;
    }
}
