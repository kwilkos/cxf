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
 
// This code is structured on to require a 'new' of an object of type CxfApacheOrgUtil.
// Alternative, it could be made 'static', but this allowed us to use this same object
// to carry some state.
 
function cxf_apache_org_util_null_trace(message)
{
}
 
function CxfApacheOrgUtil()
{
	this.ELEMENT_NODE = 1;
	if ("function" == typeof(org_apache_cxf_trace)) {
		this.trace = org_apache_cxf_trace.trace;
	} else {
		this.trace = cxf_apache_org_util_null_trace;
    }		
}

// compensate for Microsoft's weakness here.
function org_apache_cxf_getNodeLocalName(node)
{
    if("localName" in node) {
        return node.localName;
    } else {
        return node.baseName;
    }
}

CxfApacheOrgUtil.prototype.getNodeLocalName = org_apache_cxf_getNodeLocalName;

function org_apache_cxf_element_name_for_trace(node)
{
	if(node == null)
		return "null";
	else if(node == undefined)
		return "undefined";
	else {
	    var n = '';
	    if(node.namespaceURI != null && node.namespaceURI != '') {
   			n = n + "{" + node.namespaceURI + "}";
   		} 
   		return n + this.getNodeLocalName(node);
	}
}

CxfApacheOrgUtil.prototype.traceElementName = org_apache_cxf_element_name_for_trace; 

function org_apache_cxf_escapeXmlEntities(val) {
    if(val == null)
        return "";
    else
        return val.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

CxfApacheOrgUtil.prototype.escapeXmlEntities = org_apache_cxf_escapeXmlEntities; 
    
function org_apache_cxf_isElementNil(node) {
    if(node == null)
    	throw "null node passed to isElementNil";
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
    if(node == undefined)
       throw "undefined node to getFirstElementChild";

	var n;
	for(n = node.firstChild; n != null && n.nodeType != this.ELEMENT_NODE; n = n.nextSibling) {
	}
		
	return n;
}

CxfApacheOrgUtil.prototype.getFirstElementChild = org_apache_cxf_getFirstElementChild; 

function org_apache_cxf_getNextElementSibling(node) {
	if(node == undefined)
		throw "undefined node to getNextElementSibling";
	if(node == null)
		throw "null node to getNextElementSibling";
	var n;
	for(n = node.nextSibling; n != null && n.nodeType != this.ELEMENT_NODE; n = n.nextSibling)
		;
	return n;
}

CxfApacheOrgUtil.prototype.getNextElementSibling = org_apache_cxf_getNextElementSibling; 

function org_apache_cxf_isNodeNamedNS(node, namespaceURI, localName)
{
    if(node == undefined)
       throw "undefined node to isNodeNamedNS";

    if(namespaceURI == '' || namespaceURI == null) {
        if(node.namespaceURI == '' || node.namespaceURI == null) {
            return localName == org_apache_cxf_getNodeLocalName(node);
        } else
            return false;
    } else {
        return namespaceURI == node.namespaceURI && localName == org_apache_cxf_getNodeLocalName(node);
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

// The following could be parameterized using the SoapVersion class, but does anyone believe that
// there will ever be another soap version?

function org_apache_cxf_begin_soap11_message(namespaceAttributes)
{
	return
		'<?xml version="1.0" encoding="UTF-8"?><soap-env:Envelope xmlns:soap-env="http://schemas.xmlsoap.org/soap/envelope/"'
		+ ' xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"'
	    + '><soap-env:Body '
	    + namespaceAtttributes 
	    + '>';
}
CxfApacheOrgUtil.prototype.beginSoap11Message = org_apache_cxf_begin_soap11_message; 

function org_apache_cxf_end_soap11_message()
{
	return '</soap-env:Body>';
}
	
CxfApacheOrgUtil.prototype.envSoap11Message = org_apache_cxf_end_soap11_message; 
	
