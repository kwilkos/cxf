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

package org.apache.cxf.configuration.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

public class JaxbPropertyEditorRegistrar implements PropertyEditorRegistrar {
    private String packageName;
    private List<String> propertyClassNames;

    public JaxbPropertyEditorRegistrar() {
        propertyClassNames = new ArrayList<String>();
    }

    public List<String> getPropertyClassNames() {
        return propertyClassNames;
    }

    public void setPropertyClassNames(List<String> propertyClassNames) {
        this.propertyClassNames = propertyClassNames;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void registerCustomEditors(PropertyEditorRegistry registry) {
        if (null != propertyClassNames && propertyClassNames.size() > 0) {
            JaxbPropertyEditor editor = new JaxbPropertyEditor();
            editor.setPackageName(packageName);
            for (String s : propertyClassNames) {
                String fullClassName = packageName + "." + s;
                Class cls = null;
                try {
                    cls = ClassLoaderUtils.loadClass(fullClassName, this.getClass());
                } catch (ClassNotFoundException ex) {
                    // TODO
                    ex.printStackTrace();
                }
                if (null != cls) {
                    registry.registerCustomEditor(cls, editor);
                }
            }
        }
    }
}
