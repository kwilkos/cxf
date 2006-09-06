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

package org.apache.cxf.tools.wsdl2java.processor.internal;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.util.ProcessorUtil;

public class JAXBBindingMerger {   
    protected static final Logger LOG = LogUtils.getL7dLogger(JAXBBindingMerger.class);
    private boolean merged;
    public void mergeJaxwsBinding(Element schema, ToolContext env) {
        String[] bindingFiles;
        try {
            bindingFiles = (String[])env.get(ToolConstants.CFG_BINDING);
        } catch (ClassCastException e) {
            bindingFiles = new String[1];
            bindingFiles[0] = (String)env.get(ToolConstants.CFG_BINDING);
        }
       
        if (bindingFiles == null) {
            return;
        }
        
        for (int i = 0; i < bindingFiles.length; i++) {
            File jaxwsFile = new File(bindingFiles[i]);
            // get jaxBidnings nested in jaxws bindings
            NodeList jaxbBindingList = getJaxbBindingsNodes(jaxwsFile);
            String elementpath = ".";
            if (jaxbBindingList != null && jaxbBindingList.getLength() > 0) {
                for (int j = 0; j < jaxbBindingList.getLength(); j++) {
                    Node bindingNode = jaxbBindingList.item(j);
                    elementpath = ((Element)bindingNode).getAttribute("node");

                    NamespaceContext nsContext = (NamespaceContext)new NamespaceContextImpl(jaxwsFile);
                    NodeList bindNodesInSchemaElement = findBindingNode(schema, elementpath, nsContext);

                    if (bindNodesInSchemaElement != null && bindNodesInSchemaElement.getLength() > 0) {
                        for (int k = 0; k < bindNodesInSchemaElement.getLength(); k++) {
                            // set jaxb namespace and jaxb version attribute
                            String jaxbPrefix = schema.lookupPrefix("http://java.sun.com/xml/ns/jaxb");
                            if (jaxbPrefix == null) {
                                schema.setAttribute("xmlns:jaxb", "http://java.sun.com/xml/ns/jaxb");
                                schema.setAttribute("jaxb:version", "2.0");
                            }
                            // bindElement in schema
                            Element bindElement = (Element)bindNodesInSchemaElement.item(k);
                            NodeList jaxbBindings = bindingNode.getChildNodes();
                            if (jaxbBindings.getLength() != 0) {
                                Element annoElement = 
                                    bindElement.getOwnerDocument().createElementNS(ToolConstants.SCHEMA_URI, 
                                                                                   "annotation");
                                Element appinfoEle = 
                                    bindElement.getOwnerDocument().createElementNS(ToolConstants.SCHEMA_URI, 
                                                                                   "appinfo");
                                annoElement.appendChild(appinfoEle);
                                for (int l = 0; l < jaxbBindings.getLength(); l++) {
                                    Node jaxbBindingNode = jaxbBindings.item(l);
                                    Node node = ProcessorUtil.cloneNode(bindElement.getOwnerDocument(), 
                                                                jaxbBindingNode, true);
                                    appinfoEle.appendChild(node);
                                }
                                if (bindElement.getChildNodes().getLength() > 0) {
                                    bindElement
                                        .insertBefore(annoElement, bindElement.getChildNodes().item(0));
                                } else {
                                    bindElement.appendChild(annoElement);
                                }
                                merged = true;
                            }
                           
                        }
                    }
                }
            }

        }
        
        
    }

    private NodeList findBindingNode(Element schemaElement, String bindingPath, NamespaceContext nsContext) {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        javax.xml.xpath.XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(nsContext);
        NodeList nodelist = null;
        try {
            nodelist = (NodeList)xpath.evaluate(bindingPath, schemaElement,
                                                javax.xml.xpath.XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodelist;
    }

    private NodeList getJaxbBindingsNodes(File jaxwsFile) {
        DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
        docBuilderFact.setNamespaceAware(true);
        Document jaxwsDoc;
        NodeList nodeList = null;

        try {
            jaxwsDoc = docBuilderFact.newDocumentBuilder().parse(jaxwsFile);
            NodeList jaxwsBindingNodeList = jaxwsDoc.getElementsByTagNameNS(ToolConstants.NS_JAXWS_BINDINGS,
                                                                            "bindings");

            if (jaxwsBindingNodeList.getLength() == 0) {
                return null;
            }
            nodeList = jaxwsDoc.getElementsByTagNameNS(ToolConstants.NS_JAXB_BINDINGS, "bindings");
        } catch (Exception e) {
            org.apache.cxf.common.i18n.Message msg = 
                new org.apache.cxf.common.i18n.Message("FAIL_TO_GET_JAXBINDINGNODE_FROM_JAXWSBINDING",
                                                             LOG);
            LOG.log(Level.SEVERE, msg.toString());
            throw new ToolException(msg, e);
        } 
        return nodeList;
    }
    
    public boolean isMerged() {
        return merged;
    }
    
}
