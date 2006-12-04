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

package org.apache.cxf.xjc.cfg;

import java.util.Collections;
import java.util.List;

import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

import org.apache.cxf.configuration.AbstractConfigurableBeanBase;

/**
 * Modifies the JAXB code model to initialise fields mapped from schema elements
 * with their default value.
 */
public class ConfigurableBeansPlugin extends Plugin {

    private static final String CFG_NAMESPACE_URI = "http://cxf.apache.org/configuration/cfg";
    private static final String CFG_CONFIGURABLE_ELEM_NAME = "configurable";

    public ConfigurableBeansPlugin() {
    }    

    public String getOptionName() {
        return "Xcfg";
    }

    public String getUsage() {
        return "-Xcfg: Generate configurable beans.";
    }

    public List<String> getCustomizationURIs() {
        return Collections.singletonList(CFG_NAMESPACE_URI);
    }

    public boolean isCustomizationTagName(String nsUri, String localName) {
        return nsUri.equals(CFG_NAMESPACE_URI) && localName.equals(CFG_CONFIGURABLE_ELEM_NAME);
    }

    public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) {
       
        for (ClassOutline co : outline.getClasses()) {
            CPluginCustomization cust = co.target.getCustomizations().find(CFG_NAMESPACE_URI,
                                                                           CFG_CONFIGURABLE_ELEM_NAME);
            if (null == cust) {
                continue;
            }

            cust.markAsAcknowledged();

            // generated class extends AbstractConfigurableBeanBase

            JDefinedClass dc = co.implClass;
            dc._extends(AbstractConfigurableBeanBase.class);
            
            // replace default getters by getters trying the registered providers

            updateGetters(co, dc);
            
            // modify default setters to notify property change

            updateSetters(co, dc);
        }

        return true;
    }
    
    private void updateGetters(ClassOutline co, JDefinedClass dc) {
        for (FieldOutline fo : co.getDeclaredFields()) {

            String fieldName = fo.getPropertyInfo().getName(false);
            JType type = fo.getRawType();
            String typeName = type.fullName();
            
            String getterName = ("java.lang.Boolean".equals(typeName) ? "is" : "get")
                                + fo.getPropertyInfo().getName(true);
            
            // retain existing javadoc, modifiers, and name
            
            JMethod method = dc.getMethod(getterName, new JType[0]);
            JDocComment doc = method.javadoc();
            int mods = method.mods().getValue();
            JType mtype = method.type(); 
            JBlock block = method.body();
            
            // remove existing method and define new one
            
            dc.methods().remove(method);
            
            method = dc.method(mods, mtype, getterName);
            method.javadoc().append(doc);

            JFieldRef fr = JExpr.ref(fieldName);
            
            JExpression test;
            JConditional jc;
            JInvocation invocation;
                            
            invocation = JExpr.invoke("tryOverwrite");
            invocation.arg(JExpr.dotclass(type.boxify()));
            invocation.arg(JExpr.lit(fieldName));
            JVar tmp = method.body().decl(type.boxify(), "_" + fieldName,
                                          invocation);
            test = JOp.not(JOp.eq(JExpr._null(), tmp));
            jc = method.body()._if(test);
            jc._then()._return(tmp);
            
            test = JOp.not(JOp.eq(JExpr._null(), fr));
            jc = method.body()._if(test);
            jc._then()._return(fr);
            
            invocation = JExpr.invoke("tryFallback");
            invocation.arg(JExpr.dotclass(type.boxify()));
            invocation.arg(JExpr.lit(fieldName));
            // tmp = method.body().decl(type.boxify(), "_" + fieldName, invocation);
            method.body().assign(tmp, invocation);
            test = JOp.not(JOp.eq(JExpr._null(), tmp));
            jc = method.body()._if(test);
            jc._then()._return(tmp);
            
            // add the original block
            
            method.body().add(block);

        }
    }
    
    private void updateSetters(ClassOutline co, JDefinedClass dc) {
        for (FieldOutline fo : co.getDeclaredFields()) {

            String fieldName = fo.getPropertyInfo().getName(false);
            JType type = fo.getRawType();
            
            String setterName = "set" + fo.getPropertyInfo().getName(true);
            
            // modify the setter to notify the property change
  
            JMethod method = dc.getMethod(setterName, new JType[] {type.boxify()});
            if (null != method) {
                JInvocation invocation = JExpr.invoke("notifyPropertyChange");
                invocation.arg(JExpr.lit(fieldName));
                method.body().add(invocation);
            }
        }
    }
}
