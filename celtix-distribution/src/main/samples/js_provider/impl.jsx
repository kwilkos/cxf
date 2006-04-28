var WebServiceProvider1 = {
    'wsdlLocation': 'file:./wsdl/hello_world.wsdl',
    'serviceName': 'SOAPService1',
    'portName': 'SoapPort1',
    'targetNamespace': 'http://objectweb.org/hello_world_soap_http',
    'ServiceMode': 'MESSAGE',
};

var SOAP_ENV = new Namespace('SOAP-ENV', 'http://schemas.xmlsoap.org/soap/envelope/');
var xs = new Namespace('xs', 'http://www.w3.org/2001/XMLSchema');
var xsi = new Namespace('xsi', 'http://www.w3.org/2001/XMLSchema-instance');
var ns = new Namespace('ns', 'http://objectweb.org/hello_world_soap_http/types');

WebServiceProvider1.invoke = function(req) {
    default xml namespace = ns;
    var name = (req..requestType)[0];
    default xml namespace = SOAP_ENV;
    var resp = <SOAP-ENV:Envelope xmlns:SOAP-ENV={SOAP_ENV} xmlns:xs={xs} xmlns:xsi={xsi}/>;
    resp.Body = <Body/>;
    resp.Body.ns::greetMeResponse = <ns:greetMeResponse xmlns:ns={ns}/>;
    resp.Body.ns::greetMeResponse.ns::responseType = 'Hi ' + name;
    return resp;
}
