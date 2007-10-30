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
 
 // We use a pseudo-class for name scoping here.
 
function CxfApacheOrgUtil()
{
	this.ELEMENT_NODE = 1;
}

// compensate for Microsoft's weakness here.
function org_apache_cxf_getNodeLocalName(node)
{
    if(node.localName)
        return node.localName;
    else
        return node.baseName;
}

CxfApacheOrgUtil.prototype.getNodeLocalName = org_apache_cxf_getNodeLocalName; 


//*************************************************
//                     XML Utils
//*************************************************
function org_apache_cxf_escapeXmlEntities(val) {
    if(val == null)
        return "";
    else
        return val.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

CxfApacheOrgUtil.prototype.escapeXmlEntities = org_apache_cxf_escapeXmlEntities; 
    
function org_apache_cxf_isElementNil(node) {
    // we need to look for an attribute xsi:nil, where xsi is
    // http://www.w3.org/2001/XMLSchema-instance. we have the usual
    // problem here with namespace-awareness.
    if ('function' == typeof node.getAttributeNS) {
        var nillness = node.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "nil");
        return nillness != null && nillness == "true";
    } else { // we assume the standard prefix and hope for the best.
        var nillness = node.getAttribute("xsi:nil");
        return nillness != null && nillness == "true";
    }
}

CxfApacheOrgUtil.prototype.isElementNil = org_apache_cxf_isElementNil; 

function org_apache_cxf_getFirstElementChild(node) {
	var n;
	for(n = node.firstChild; n != null && n.nodeType !=  this.ELEMENT_NODE; n = n.nextSibling) {
	}
	return n;
}

CxfApacheOrgUtil.prototype.getFirstElementChild = org_apache_cxf_getFirstElementChild; 

function org_apache_cxf_getNextElementSibling(node) {
	var n;
	for(n = node.nextSibling; n != null && n.nodeType != this.ELEMENT_NODE; n = n.nextSibling)
		;
	return n;
}

CxfApacheOrgUtil.prototype.getNextElementSibling = org_apache_cxf_getNextElementSibling; 

function org_apache_cxf_isNodeNamedNS(node, namespaceURI, localName)
{
    if(namespaceURI == '' || namespaceURI == null) {
        if(node.namespaceURI == '' || node.namespaceURI == null) {
            return localName == xml_getLocalName(node);
        } else
            return false;
    } else {
        return namespaceURI == node.namespaceURI && localName == xml_getLocalName(node);
    }
}

CxfApacheOrgUtil.prototype.isNodeNamedNS = org_apache_cxf_isNodeNamedNS; 

//Firefox splits large text regions into multiple Text objects (4096 chars in each).
function org_apache_cxf_getNodeText(node)
{
    var r = "";
    for(x = 0; x < node.childNodes.length; x ++) {
        r = r + node.childNodes[x].nodeValue;
    }
    return r;
}

CxfApacheOrgUtil.prototype.getNodeText = org_apache_cxf_getNodeText;
