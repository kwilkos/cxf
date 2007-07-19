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
package org.apache.cxf.systest.jaxws;

import java.util.Arrays;
import java.util.List;

import javax.jws.WebService;

@WebService(endpointInterface = "org.apache.cxf.systest.jaxws.DocLitWrappedCodeFirstService",
            serviceName = "DocLitWrappedCodeFirstService",
            portName = "DocLitWrappedCodeFirstServicePort",
            targetNamespace = "http://cxf.apache.org/systest/jaxws/DocLitWrappedCodeFirstService")
public class DocLitWrappedCodeFirstServiceImpl implements DocLitWrappedCodeFirstService {
    public static final String DATA[] = new String[] {"string1", "string2", "string3"};
    
    public String[] arrayOutput() {
        return DATA;
    }

    public List<String> listOutput() {
        return Arrays.asList(DATA);
    }

    public String arrayInput(String[] inputs) {
        StringBuffer buf = new StringBuffer();
        for (String s : inputs) {
            buf.append(s);
        }
        return buf.toString();
    }

    public String listInput(List<String> inputs) {
        StringBuffer buf = new StringBuffer();
        for (String s : inputs) {
            buf.append(s);
        }
        return buf.toString();
    }
    
    public String multiListInput(List<String> inputs1, List<String> inputs2, String x, int y) {
        StringBuffer buf = new StringBuffer();
        for (String s : inputs1) {
            buf.append(s);
        }
        for (String s : inputs2) {
            buf.append(s);
        }
        if (x == null) {
            buf.append("<null>");
        } else {
            buf.append(x);
        }
        buf.append(Integer.toString(y));
        return buf.toString();
    }

}
