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

package org.apache.cxf.tools.java2wsdl.generator.wsdl11.annotator;

import org.apache.cxf.tools.common.model.Annotator;
import org.apache.cxf.tools.common.model.JavaAnnotatable;
import org.apache.cxf.tools.common.model.JavaAnnotation;
import org.apache.cxf.tools.common.model.JavaField;

public class WrapperBeanFieldAnnotator implements Annotator {

    public void annotate(final JavaAnnotatable field) {
        JavaField jField = null;
        if (field instanceof JavaField) {
            jField = (JavaField) field;
        } else {
            throw new RuntimeException("WrapperBeanFiledAnnotator expect JavaField as input");
        }
        String rawName = jField.getRawName();
        JavaAnnotation xmlElementAnnotation = new JavaAnnotation("XmlElement");
        
        xmlElementAnnotation.addArgument("name", rawName);
        xmlElementAnnotation.addArgument("namespace", jField.getTargetNamespace());

        jField.setAnnotation(xmlElementAnnotation);
        jField.getOwner().addImport("javax.xml.bind.annotation.XmlElement");
    }
}
