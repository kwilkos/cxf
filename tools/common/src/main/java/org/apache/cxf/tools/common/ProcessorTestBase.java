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

package org.apache.cxf.tools.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.cxf.helpers.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public class ProcessorTestBase extends Assert {

    protected ToolContext env = new ToolContext();
    protected File output;

    @Before
    public void setUp() throws Exception {
        URL url = getClass().getResource(".");
        output = new File(url.toURI());
        output = new File(output, "/resources");
        FileUtils.mkDir(output);
    }

    
    @After
    public void tearDown() {
        FileUtils.removeDir(output);
        output = null;
        env = null;
    }

    protected String getClassPath() throws URISyntaxException {
        ClassLoader loader = getClass().getClassLoader();
        StringBuffer classPath = new StringBuffer();
        if (loader instanceof URLClassLoader) {
            URLClassLoader urlLoader = (URLClassLoader)loader;
            for (URL url : urlLoader.getURLs()) {               
                File file;
                file = new File(url.toURI());
                String filename = file.getAbsolutePath();                
                if (filename.indexOf("junit") == -1) {
                    classPath.append(filename);
                    classPath.append(System.getProperty("path.separator"));
                }
            }
        }
        return classPath.toString();
    }
    
    protected String getLocation(String wsdlFile) throws URISyntaxException {
        return getClass().getResource(wsdlFile).toString();
    }

    protected void assertFileEquals(String f1, String f2) {
        assertFileEquals(new File(f1), new File(f2));
    }
    
    protected void assertFileEquals(File location1, File location2) {
        String str1 = getStringFromFile(location1);
        String str2 = getStringFromFile(location2);
        
        StringTokenizer st1 = new StringTokenizer(str1, " \t\n\r\f(),");
        StringTokenizer st2 = new StringTokenizer(str2, " \t\n\r\f(),");

        // namespace declarations and wsdl message parts can be ordered
        // differently in the generated wsdl between the ibm and sun jdks.
        // So, when we encounter a mismatch, put the unmatched token in a
        // list and check this list when matching subsequent tokens.
        // It would be much better to do a proper xml comparison.
        List<String> unmatched = new ArrayList<String>();
        while (st1.hasMoreTokens()) {
            String tok1 = st1.nextToken();
            String tok2 = null;
            if (unmatched.contains(tok1)) {
                unmatched.remove(tok1);
                continue;
            }
            while (st2.hasMoreTokens()) {
                tok2 = st2.nextToken();

                if (tok1.equals(tok2)) {
                    break;
                } else {
                    unmatched.add(tok2);
                }
            }
            assertEquals("Compare failed " + location1.getAbsolutePath() 
                         + " != " + location2.getAbsolutePath(), tok1, tok2);
        }

        assertTrue(!st1.hasMoreTokens());
        assertTrue(!st2.hasMoreTokens());
        assertTrue("Files did not match", unmatched.isEmpty());
    }

    public String getStringFromFile(File location) {
        InputStream is = null;
        String result = null;

        try {
            is = new FileInputStream(location);
            result = normalizeCRLF(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    //do nothing
                }
            }
        }

        return result;
    }
    
    private String normalizeCRLF(InputStream instream) {
        BufferedReader in = new BufferedReader(new InputStreamReader(instream));
        StringBuffer result = new StringBuffer();
        String line = null;

        try {
            line = in.readLine();
            while (line != null) {
                String[] tok = line.split("\\s");

                for (int x = 0; x < tok.length; x++) {
                    String token = tok[x];
                    result.append("  " + token);
                }
                line = in.readLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String rtn = result.toString();

        rtn = ignoreTokens(rtn, "<!--", "-->");
        rtn = ignoreTokens(rtn, "/*", "*/");
        return rtn;
    }

    private String ignoreTokens(final String contents, final String startToken, final String endToken) {
        String rtn = contents;
        int headerIndexStart = rtn.indexOf(startToken);
        int headerIndexEnd = rtn.indexOf(endToken);
        if (headerIndexStart != -1 && headerIndexEnd != -1 && headerIndexStart < headerIndexEnd) {
            rtn = rtn.substring(0, headerIndexStart - 1)
                + rtn.substring(headerIndexEnd + endToken.length() + 1);
        }
        return rtn;
    }
}
