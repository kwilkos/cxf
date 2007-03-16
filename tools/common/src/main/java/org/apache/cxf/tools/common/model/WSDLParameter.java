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

package org.apache.cxf.tools.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.sun.xml.bind.api.TypeReference;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaType.Style;
public class WSDLParameter {
    private static final Logger LOG = LogUtils.getL7dLogger(WSDLParameter .class);
    protected String name;
    protected String type;
    protected String className;
    protected String targetNamespace;
    protected final List<JavaParameter> parts = new ArrayList<JavaParameter>();
    private TypeReference typeRef;
    private Style style;
    private String pname;
    
    public WSDLParameter() {
        
    }

    public WSDLParameter(String paraName, TypeReference ref, Style paraStyle) {
        pname = paraName;
        typeRef = ref;
        style = paraStyle;
    }
    
    public void setName(String arg) {
        this.pname = arg;
    }

    public void setClassName(String clzName) {
        this.className = clzName;
    }

    public String getClassName() {
        return this.className;
    }

    public void setTargetNamespace(String tns) {
        this.targetNamespace = tns;
    }

    public String getTargetNamespace() {
        return this.targetNamespace;
    }

    public Style getStyle() {
        return this.style;
    }

    public void setStyle(Style s) {
        this.style = s;
    }

    public String getName() {
        return pname;
    }

    

    public void setTypeReference(TypeReference ref) {
        this.typeRef = ref;
    }

    public TypeReference getTypeReference() {
        return this.typeRef;
    }

    public boolean isWrapped() {
        return true;
    }

    public List<JavaParameter> getChildren() {
        return Collections.unmodifiableList(parts);
    }

    public void addChildren(JavaParameter jp) {
        if (parts.contains(jp)) {
            Message message = new Message("PART_ALREADY_EXIST", LOG, jp.getName());
            throw new ToolException(message);
        }
        parts.add(jp);
    }

    public JavaParameter removeChildren(int index) {
        return parts.remove(index);
    }

    public void clear() {
        parts.clear();
    }

   
}
