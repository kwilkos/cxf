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

package org.apache.cxf.tools.java2wsdl.generator.wsdl11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import org.apache.cxf.tools.common.model.JavaClass;

public final class JCodeModelFilter {
    final JCodeModel model;

    public JCodeModelFilter(JCodeModel m) {
        this.model = m;
    }

    public void include(List<JavaClass> included) {
        Set<String> includedPackages = new HashSet<String>();
        List<String> includedClasses = new ArrayList<String>();
        
        for (JavaClass clz : included) {
            includedPackages.add(clz.getPackageName());
            includedClasses.add(clz.getFullClassName());
        }

        for (Iterator<JPackage> iter = this.model.packages(); iter.hasNext();) {
            JPackage pkg = iter.next();
            if (!includedPackages.contains(pkg.name())) {
                remove(pkg);
            } else {
                remove(pkg, includedClasses);
            }
        }
    }

    private void remove(JPackage pkg, List<String> includedClasses) {
        List<JDefinedClass> toRemove = new ArrayList<JDefinedClass>();
        for (Iterator<JDefinedClass> iter = pkg.classes(); iter.hasNext();) {
            JDefinedClass clz = iter.next();
            if (!includedClasses.contains(pkg.name() + "." + clz.name())) {
                toRemove.add(clz);
            }
        }
        for (JDefinedClass clz : toRemove) {
            pkg.remove(clz);
        }
    }
    
    private void remove(JPackage pkg) {
        remove(pkg, new ArrayList<String>());
    }
}
