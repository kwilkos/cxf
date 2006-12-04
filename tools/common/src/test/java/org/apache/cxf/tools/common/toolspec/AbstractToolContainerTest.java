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

package org.apache.cxf.tools.common.toolspec;

import junit.framework.TestCase;
import org.apache.cxf.tools.common.ToolException;
public class AbstractToolContainerTest extends TestCase {
    private DummyToolContainer dummyTool;

    public AbstractToolContainerTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        String tsSource = "/org/apache/cxf/tools/common/toolspec/parser/resources/testtool.xml";
        ToolSpec toolspec = new ToolSpec(getClass().getResourceAsStream(tsSource), false);
        dummyTool = new DummyToolContainer(toolspec);
    }

    public void testQuietMode() {
        // catch all exception in here.
        try {
            dummyTool.setCommandLine(new String[] {"-q"});
        } catch (Exception e) {
            // caught expected exception
        }
        assertNotNull("Fail to redirect err output:", dummyTool.getErrOutputStream());
        assertNotNull("Fail to redirect output:", dummyTool.getOutOutputStream());
    }

    public void testInit() {
        try {
            dummyTool.init();
        } catch (ToolException e) {
            assertEquals("Tool specification has to be set before initializing", e.getMessage());
            return;
        }
        assertTrue(true);
    }

    public void testToolRunner() throws Exception {
        String tsSource = "/org/apache/cxf/tools/common/toolspec/parser/resources/testtool.xml";
        String[] args = {"-r", "wsdlurl=dfd"};
        ToolRunner.runTool(DummyToolContainer.class, getClass().getResourceAsStream(tsSource), false, args);
    }
}
