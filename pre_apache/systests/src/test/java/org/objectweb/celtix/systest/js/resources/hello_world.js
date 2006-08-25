var WebServiceProvider = {
    'wsdlLocation': 'file:../testutils/target/classes/wsdl/hello_world.wsdl',
    'serviceName': 'SOAPService',
    'portName': 'SoapPort',
    'targetNamespace': 'http://objectweb.org/hello_world_soap_http',
    'ServiceMode': 'MESSAGE',
};

var SOAP_ENV = "http://schemas.xmlsoap.org/soap/envelope/";
var ns4 = "http://objectweb.org/hello_world_soap_http/types";

WebServiceProvider.invoke = function(req) {
    var resp = req.getImplementation().createDocument(SOAP_ENV, "SOAP-ENV:Envelope", null);
    var list = req.getElementsByTagNameNS(ns4, "greetMe");
    var txt, responseNode;
    if (list.length > 0) {
        txt = resp.createTextNode("TestGreetMeResponse");
        responseNode = 'ns4:greetMeResponse';
    } else {
        txt = resp.createTextNode("TestSayHiResponse");
        responseNode = 'ns4:sayHiResponse';
    }
    var respType = resp.createElementNS(ns4, "ns4:responseType");
    respType.insertBefore(txt, null);
    var gm = resp.createElementNS(ns4, responseNode);
    gm.insertBefore(respType, null);
    var sb = resp.createElementNS(SOAP_ENV, "SOAP-ENV:Body");
    sb.insertBefore(gm, null);
    resp.getDocumentElement().insertBefore(sb, null);
    return resp;
}
