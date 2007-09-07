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

import org.apache.cxf.tools.util.URIParserUtil;

public class JavaField extends JavaType implements JavaAnnotatable {
    private String modifier;
    private JavaAnnotation annotation;

    public JavaField() {
    }

    public JavaField(String n, String t, String tns) {
        super(n, t, tns);
        this.modifier = "private";
    }

    public String getModifier() {
        return this.modifier;
    }

    public void setModifier(String modi) {
        this.modifier = modi;
    }

    public void setAnnotation(JavaAnnotation anno) {
        this.annotation = anno;
    }

    public JavaAnnotation getAnnotation() {
        return this.annotation;
    }

    public void annotate(Annotator annotator) {
        annotator.annotate(this);
    }

    public String getName() {
        if (URIParserUtil.containsReservedKeywords(this.name)) {
            return "_" + this.name;
        }
        return this.name;
    }
    
}
