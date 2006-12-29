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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.annotator;

import java.util.Map;

import junit.framework.TestCase;

import org.apache.cxf.tools.common.model.JavaAnnotation;
import org.apache.cxf.tools.common.model.JavaMethod;

public class WebMethodAnnotatorTest extends TestCase {

    WebMethodAnnotator annotator = new WebMethodAnnotator();
    
    public void testAddWebMethodAnnotation() throws Exception {
        JavaMethod method = new JavaMethod();
        annotator.addWebMethodAnnotation(method);
        Map<String, JavaAnnotation> annotations = method.getAnnotationMap();
        assertNotNull(annotations);
        assertEquals(1, annotations.size());
        assertEquals("WebMethod", annotations.keySet().iterator().next());
    }

    public void testAddWebResultAnnotation() throws Exception {
        JavaMethod method = new JavaMethod();
        annotator.addWebResultAnnotation(method);
        Map<String, JavaAnnotation> annotations = method.getAnnotationMap();
        assertNotNull(annotations);
        assertEquals(1, annotations.size());
        assertEquals("WebResult", annotations.keySet().iterator().next());
        JavaAnnotation resultAnnotation = annotations.values().iterator().next();
        Map<String, String> arguments = resultAnnotation.getArguments();
        assertNotNull(arguments);
        assertEquals(1, arguments.size());
        assertEquals("name", arguments.keySet().iterator().next());
        assertEquals("\"return\"", arguments.values().iterator().next());
    }
}
