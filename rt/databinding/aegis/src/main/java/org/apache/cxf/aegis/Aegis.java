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
package org.apache.cxf.aegis;

import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.util.NamespaceHelper;
import org.apache.cxf.aegis.util.XmlConstants;

/**
 * Static methods/constants for Aegis.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public class Aegis {
    public static final Log LOG = LogFactory.getLog(Aegis.class);

    public static final String CURRENT_MESSAGE_PART = "currentMessagePart";

    public static final String TYPE_MAPPING_KEY = "type.mapping";

    public static final String ENCODING_URI_KEY = "type.encodingUri";

    public static final String WRITE_XSI_TYPE_KEY = "writeXsiType";

    public static final String OVERRIDE_TYPES_KEY = "overrideTypesList";

    public static Type getReadType(XMLStreamReader xsr, Context context, Type type) {
        String overrideType = xsr.getAttributeValue(XmlConstants.XSI_NS, "type");
        if (overrideType != null) {
            QName overrideTypeName = NamespaceHelper.createQName(xsr.getNamespaceContext(), overrideType);
            if (!overrideTypeName.equals(type.getSchemaType())) {
                Type type2 = type.getTypeMapping().getType(overrideTypeName);
                if (type2 == null) {
                    LOG.info("xsi:type=\"" + overrideTypeName
                             + "\" was specified, but no corresponding Type was registered; defaulting to "
                             + type.getSchemaType());
                } else {
                    type = type2;
                }
            }
        }
        return type;
    }

    public static Type getWriteType(Context context, Object value, Type type) {
        if (value != null && type != null && type.getTypeClass() != value.getClass()) {
            List l = (List) context.get(OVERRIDE_TYPES_KEY);
            if (l != null && l.contains(value.getClass().getName())) {
                type = type.getTypeMapping().getType(value.getClass());
            }
        }
        return type;
    }

}
