/**
 **/
var WebServiceProvider = {
    "wsdlLocation": "file:./wsdl/hello_world.wsdl",
    "serviceName": "SOAPService3",
    "portName": "SoapPort3",
    "targetNamespace": "http://apache.org/hello_world_soap_http",
};

WebServiceProvider.invoke = function(document) {
    var ns4 = "http://apache.org/hello_world_soap_http/types";
    var list = document.getElementsByTagNameNS(ns4, "requestType");
    var name = list.item(0).getFirstChild().getNodeValue();
    var newDoc = document.getImplementation().createDocument(ns4, "ns4:greetMeResponse", null);
    var el = newDoc.createElementNS(ns4, "ns4:responseType");
    var txt = newDoc.createTextNode("Hi " + name);
    el.insertBefore(txt, null);
    newDoc.getDocumentElement().insertBefore(el, null);
    return newDoc;
}
