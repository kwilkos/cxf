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

package org.apache.cxf.javascript;

import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * A Rhino wrapper around org.w3c.dom.Node. Not comprehensive, but enough to test CXF JavaScript. 
 */
public class JsSimpleDomNode extends ScriptableObject {
    private Node wrappedNode;
    private boolean childrenWrapped;
    private JsSimpleDomNode previousSibling;
    private JsSimpleDomNode nextSibling;
    private JsSimpleDomNode[] children;

    /**
     * Only exists to make Rhino happy. Should never be used.
     */
    public JsSimpleDomNode() {
    }
    
    public static void register(ScriptableObject scope) {
        try {
            ScriptableObject.defineClass(scope, JsSimpleDomNode.class);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String getClassName() {
        return "Node";
    }
    
    //CHECKSTYLE:OFF
    public String jsGet_localName() {
        return wrappedNode.getLocalName();       
    }
    
    public String jsGet_namespaceURI() {
        return wrappedNode.getNamespaceURI();
    }
    
    public Object jsGet_firstChild() {
        establishChildren();
        if (children.length > 0)
            return children[0];
        else 
            return null;
    }

    public Object jsGet_nextSibling() {
        return nextSibling; 
    }

    public Object jsGet_previousSibling() {
        return previousSibling; 
    }
    
    public int jsGet_nodeType() {
        return wrappedNode.getNodeType();
    }
    
    public String jsGet_nodeValue() {
        return wrappedNode.getNodeValue();
    }
    
    public Object[] jsGet_childNodes() {
        establishChildren();
        return children;
    }
    
    public String jsFunction_getAttributeNS(String namespaceURI, String localName) {
        NamedNodeMap attributes = wrappedNode.getAttributes();
        Node attrNode = attributes.getNamedItemNS(namespaceURI, localName);
        if(attrNode == null) {
            return null;
        } else {
            Attr attribute = (Attr) attrNode;
            return attribute.getValue();
        }
    }

    public String jsFunction_getAttribute(String localName) {
        NamedNodeMap attributes = wrappedNode.getAttributes();
        Node attrNode = attributes.getNamedItem(localName);
        if(attrNode == null) {
            return null;
        } else {
            Attr attribute = (Attr) attrNode;
            return attribute.getValue();
        }
    }

    //CHECKSTYLE:ON
    
    private JsSimpleDomNode newObject(Node node, JsSimpleDomNode prev) {
        Context cx = Context.enter();
        JsSimpleDomNode newObject = (JsSimpleDomNode)cx.newObject(getParentScope(), "Node");
        newObject.initialize(node, prev);
        return newObject;
    }

    private void establishChildren() {
        if (!childrenWrapped) {
            if (wrappedNode.hasChildNodes()) {
                NodeList nodeChildren = wrappedNode.getChildNodes();
                children = new JsSimpleDomNode[nodeChildren.getLength()];
                for (int x = 0; x < nodeChildren.getLength(); x++) {
                    JsSimpleDomNode prev = null;
                    if (x > 0) {
                        prev = (JsSimpleDomNode)children[x - 1]; 
                    }
                    children[x] = newObject(nodeChildren.item(x), prev);
                    if (x > 0) {
                        children[x - 1].setNext(children[x]);
                    }
                }
            } else {
                children = new JsSimpleDomNode[0];
            }
            childrenWrapped = true;
        }
    }

    //rhino won't let us use a constructor.
    void initialize(Node node, JsSimpleDomNode prev) {
        wrappedNode = node;
        childrenWrapped = false;
        previousSibling = prev;
    }
    
    void setNext(JsSimpleDomNode next)  {
        nextSibling = next;
    }


}
