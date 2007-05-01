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


public class JavaParameter extends JavaType implements JavaAnnotatable {

    private boolean holder;
    private String holderName;
    private String holderClass;
    private JavaAnnotation annotation;
    private String partName;

    private JavaMethod javaMethod;
    
    public JavaParameter() {
    }

    public JavaParameter(String n, String t, String tns) {
        super(n, t, tns);
    }

    public boolean isHolder() {
        return holder;
    }

    public void setHolder(boolean b) {
        holder = b;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String hn) {
        this.holderName = hn;
    }

    public void setHolderClass(String clz) {
        this.holderClass = clz;
    }

    public String getHolderClass() {
        return this.holderClass;
    }

    public void setAnnotation(JavaAnnotation anno) {
        this.annotation = anno;
    }

    public JavaAnnotation getAnnotation() {
        return this.annotation;
    }

    public void setPartName(String name) {
        this.partName = name;
    }
    
    public String getPartName() {
        return this.partName;
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        if (holder) {
            sb.append("\nIS Holder: [Holder Name]:");
            sb.append(holderName);
        }
        if (isHeader()) {
            sb.append("\nIS Header");
        }
        sb.append("\n Annotation:");
        sb.append(annotation);
        
        sb.append("\n PartName");
        sb.append(partName);
        return sb.toString();
    }
    
    public String getDefaultTypeValue() {
        if (isHolder()) {
            StringBuffer sb = new StringBuffer();
            sb.append("new ");
            sb.append(getHolderName());
            sb.append("<");
            sb.append(getHolderClass());
            sb.append(">()");
            return sb.toString(); 
        }
        return super.getDefaultTypeValue();
    }

    public void setMethod(JavaMethod jm) {
        this.javaMethod = jm;
    }

    public JavaMethod getMethod() {
        return this.javaMethod;
    }

    public void annotate(Annotator annotator) {
        annotator.annotate(this);
    }
}
