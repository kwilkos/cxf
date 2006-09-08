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
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
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
import com.sun.tools.xjc.util.NamespaceContextAdapter;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XmlString;

import org.apache.cxf.configuration.AbstractConfigurableBeanBase;

/**
 * Modifies the JAXB code model to initialise fields mapped from schema elements
 * with their default value.
 */
public class ConfigurableBeansPlugin extends Plugin {

    private static final String CFG_NAMESPACE_URI = "http://cxf.apache.org/configuration/cfg";
    private static final String CFG_CONFIGURATION_ELEM_NAME = "configuration";

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
        return nsUri.equals(CFG_NAMESPACE_URI) && localName.equals(CFG_CONFIGURATION_ELEM_NAME);
    }

    public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) {
        System.out.println("Running configurable beans plugin.");
       
        for (ClassOutline co : outline.getClasses()) {
            CPluginCustomization cust = co.target.getCustomizations().find(CFG_NAMESPACE_URI,
                                                                           CFG_CONFIGURATION_ELEM_NAME);
            if (null == cust) {
                continue;
            }

            cust.markAsAcknowledged();

            // generated class extends AbstractConfigurableBeanBase

            JDefinedClass dc = co.implClass;
            dc._extends(AbstractConfigurableBeanBase.class);
            
            // set default values

            setDefaultValues(outline);      

            // replace default getters by getters trying the registered providers

            updateGetters(co, dc);
            
            // modify default setters to notify property change

            updateSetters(co, dc);
        }

        return true;
    }
    
    private boolean setDefaultValues(Outline outline) {
        for (ClassOutline co : outline.getClasses()) {
            for (FieldOutline f : co.getDeclaredFields()) {

                // Use XML schema object model to determine if field is mapped
                // from an element or attribute with a default value
                // and get its default value.

                String fieldName = f.getPropertyInfo().getName(false);
                XmlString xmlDefaultValue = null;
                XSType xsType = null;    

                if (f.getPropertyInfo().getSchemaComponent() instanceof XSParticle) {
                    XSParticle particle = (XSParticle)f.getPropertyInfo().getSchemaComponent();
                    XSTerm term = particle.getTerm();
                    XSElementDecl element = null;

                    if (term.isElementDecl()) {
                        element = particle.getTerm().asElementDecl();
                        xmlDefaultValue = element.getDefaultValue();                        
                        xsType = element.getType();
                    }
                } else if (f.getPropertyInfo().getSchemaComponent() instanceof XSAttributeUse) {
                    XSAttributeUse attributeUse = (XSAttributeUse)f.getPropertyInfo().getSchemaComponent();
                    XSAttributeDecl decl = attributeUse.getDecl();
                    xmlDefaultValue = decl.getDefaultValue();                        
                    xsType = decl.getType();
                }

                if (null == xmlDefaultValue) {
                    continue;
                }
                
                String defaultValue = xmlDefaultValue.value;
                
                if (null == defaultValue) {
                    continue;
                }

                JType type = f.getRawType();
                String typeName = type.fullName();                

                JDefinedClass impl = co.implClass;
                Map<String, JFieldVar> fields = impl.fields();
                JFieldVar var = fields.get(fieldName);
 
                if ("java.lang.Boolean".equals(typeName)) {
                    var.init(JExpr.direct(Boolean.valueOf(defaultValue) ? "Boolean.TRUE" : "Boolean.FALSE"));
                } else if ("java.lang.Byte".equals(typeName)) {
                    var.init(JExpr._new(type)
                        .arg(JExpr.cast(type.unboxify(), 
                            JExpr.lit(new Byte(Short.valueOf(defaultValue).byteValue())))));
                } else if ("java.lang.Double".equals(typeName)) {
                    var.init(JExpr._new(type)
                        .arg(JExpr.lit(new Double(Double.valueOf(defaultValue).doubleValue()))));
                } else if ("java.lang.Float".equals(typeName)) {
                    var.init(JExpr._new(type)
                             .arg(JExpr.lit(new Float(Float.valueOf(defaultValue).floatValue()))));
                } else if ("java.lang.Integer".equals(typeName)) {
                    var.init(JExpr._new(type)
                        .arg(JExpr.lit(new Integer(Integer.valueOf(defaultValue).intValue()))));
                } else if ("java.lang.Long".equals(typeName)) {
                    var.init(JExpr._new(type)
                        .arg(JExpr.lit(new Long(Long.valueOf(defaultValue).longValue()))));
                } else if ("java.lang.Short".equals(typeName)) {
                    var.init(JExpr._new(type)
                        .arg(JExpr.cast(type.unboxify(), 
                            JExpr.lit(new Short(Short.valueOf(defaultValue).shortValue())))));
                } else if ("java.lang.String".equals(type.fullName())) {
                    var.init(JExpr.lit(defaultValue));
                } else if ("java.math.BigInteger".equals(type.fullName())) {
                    var.init(JExpr._new(type).arg(JExpr.lit(defaultValue)));
                } else if ("java.math.BigDecimal".equals(type.fullName())) {
                    var.init(JExpr._new(type).arg(JExpr.lit(defaultValue)));
                } else if ("byte[]".equals(type.fullName()) && xsType.isSimpleType()) {
                    while (!"anySimpleType".equals(xsType.getBaseType().getName())) {
                        xsType = xsType.getBaseType();
                    }
                    if ("base64Binary".equals(xsType.getName())) {
                        var.init(outline.getCodeModel().ref(DatatypeConverter.class)
                           .staticInvoke("parseBase64Binary").arg(defaultValue));
                    } else if ("hexBinary".equals(xsType.getName())) {
                        var.init(JExpr._new(outline.getCodeModel().ref(HexBinaryAdapter.class))
                            .invoke("unmarshal").arg(defaultValue));
                    }
                } else if ("javax.xml.namespace.QName".equals(typeName)) {
                    NamespaceContext nsc = new NamespaceContextAdapter(xmlDefaultValue);
                    QName qn = DatatypeConverter.parseQName(xmlDefaultValue.value, nsc);
                    var.init(JExpr._new(outline.getCodeModel().ref(QName.class))
                        .arg(qn.getNamespaceURI())
                        .arg(qn.getLocalPart())
                        .arg(qn.getPrefix()));
                }
                // TODO: GregorianCalendar, ...
            }
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
            
            // REVISIT: it seems that for Spring in order to inject a Boolean
            // the getter name needs to be get<fieldName>
            // so change it here  
            // getterName = "get" + fo.getPropertyInfo().getName(true);

            // retain existing javadoc, modifiers, and name
            
            JMethod method = dc.getMethod(getterName, new JType[0]);
            JDocComment doc = method.javadoc();
            int mods = method.mods().getValue();
            
            // ensure type is always the wrapped type
            
            JType mtype = method.type();
            
            if (mtype.isPrimitive()) {
                mtype = mtype.boxify();
            }
            
            
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
                
            test = JOp.eq(JExpr._null(), fr);
            jc = method.body()._if(test);
            invocation = JExpr.invoke("tryFallback");
            invocation.arg(JExpr.dotclass(type.boxify()));
            invocation.arg(JExpr.lit(fieldName));
            jc._then()._return(invocation);
            jc._else()._return(fr);

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
