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
import java.util.List;

import org.apache.cxf.tools.util.AnnotationUtil;

public class JavaClass extends JavaInterface {
    
    private final List<JavaField> jfield = new ArrayList<JavaField>();

    public JavaClass() {
    }
    
    public JavaClass(JavaModel model) {
        super(model);
    }

    public void addField(JavaField f) {
        this.jfield.add(f);
    }

    public List<JavaField> getFields() {
        return this.jfield;
    }

    public JavaMethod appendGetter(JavaField field) {
        String getterName = "get" + AnnotationUtil.capitalize(field.getName());
        JavaMethod jMethod = new JavaMethod(this);
        jMethod.setName(getterName);
        jMethod.setReturn(new JavaReturn(field.getName(),
                                         field.getType(),
                                         field.getTargetNamespace()));

        JavaCodeBlock block = new JavaCodeBlock();
        JavaExpression exp = new JavaExpression();
        exp.setValue("return this." + field.getName());
        block.getExpressions().add(exp);

        jMethod.setJavaCodeBlock(block);
        
        addMethod(jMethod);
        return jMethod;
    }

    public JavaMethod appendSetter(JavaField field) {
        String setterName = "set" + AnnotationUtil.capitalize(field.getName());
        JavaMethod jMethod = new JavaMethod(this);
        jMethod.setReturn(new JavaReturn("return", "void", null));
        String paramName = getSetterParamName(field.getName());
        jMethod.addParameter(new JavaParameter(paramName,
                                               field.getType(),
                                               field.getTargetNamespace()));
        JavaCodeBlock block = new JavaCodeBlock();
        JavaExpression exp = new JavaExpression();
        exp.setValue("this." + field.getName() + " = " + paramName);
        block.getExpressions().add(exp);

        jMethod.setJavaCodeBlock(block);
        
        jMethod.setName(setterName);
        addMethod(jMethod);
        return jMethod;
    }

    private String getSetterParamName(String fieldName) {
        return "new" + AnnotationUtil.capitalize(fieldName);
    }
    
}
