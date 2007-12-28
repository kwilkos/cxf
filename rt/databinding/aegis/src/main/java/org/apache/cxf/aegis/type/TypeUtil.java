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
package org.apache.cxf.aegis.type;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.aegis.AegisContext;
import org.apache.cxf.aegis.util.NamespaceHelper;
import org.apache.cxf.common.util.SOAPConstants;

/**
 * Static methods/constants for Aegis.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public final class TypeUtil {
    public static final Log LOG = LogFactory.getLog(TypeUtil.class);

    private TypeUtil() {
        //utility class
    }
    
    public static Type getReadType(XMLStreamReader xsr, AegisContext context, Type baseType) {

        if (!context.isReadXsiTypes()) {
            if (baseType == null) {
                LOG.warn("xsi:type reading disabled, and no type available for "  
                         + xsr.getName());
            }
            return baseType;
        }
        
        String overrideType = xsr.getAttributeValue(SOAPConstants.XSI_NS, "type");
        if (overrideType != null) {
            QName overrideName = NamespaceHelper.createQName(xsr.getNamespaceContext(), overrideType);

            if (baseType == null || !overrideName.equals(baseType.getSchemaType())) {
                Type improvedType = null;
                TypeMapping tm;
                if (baseType != null) {
                    tm = baseType.getTypeMapping();
                    improvedType = tm.getType(overrideName);
                }
                if (improvedType == null) {
                    improvedType = context.getOverrideType(overrideName);
                }
                if (improvedType != null) {
                    return improvedType;
                }
            }
        
            if (baseType != null) {
                LOG.info("xsi:type=\"" + overrideName
                         + "\" was specified, but no corresponding Type was registered; defaulting to "
                         + baseType.getSchemaType());
                return baseType;
            } else {
                LOG.warn("xsi:type=\"" + overrideName
                         + "\" was specified, but no corresponding Type was registered; no default.");
                return null;
            }
        } else {
            if (baseType == null) {
                LOG.warn("xsi:type absent, and no type available for "  
                         + xsr.getName());
            }
            return baseType;
        }
    }

    public static Type getWriteType(AegisContext globalContext, Object value, Type type) {
        if (value != null && type != null && type.getTypeClass() != value.getClass()) {
            Type overrideType = globalContext.getOverrideType(value.getClass());
            if (overrideType != null) {
                return overrideType;
            }
        }
        return type;
    }

}
