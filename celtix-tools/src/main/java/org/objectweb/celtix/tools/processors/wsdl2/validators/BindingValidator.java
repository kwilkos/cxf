package org.objectweb.celtix.tools.processors.wsdl2.validators;

import javax.wsdl.Definition;

public class BindingValidator {
    /*private Definition def;
    private Map<QName, List> msgPartsMap;
    private Map<QName, Map> portTypeMap;
    private Map<QName, Map> bindingMap;
    private Document document;*/
   

    public BindingValidator(Definition definition, SchemaWSDLValidator wsdlValidator) {
       /* this.def = definition;
        msgPartsMap = wsdlValidator.getMsgPartsMap();
        portTypeMap = wsdlValidator.getPortTypesMap();
        bindingMap = wsdlValidator.getBindingMap();
        document = wsdlValidator.getSchemaValidatedDoc();*/
    }

    public void vlidateBinding() {
       // return;
        /*NodeList nodelist = document.getDocumentElement().
         * getElementsByTagNameNS(.getLocalPart());
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            Node n = node.getAttributes().getNamedItem(WSDLConstants.ATTR_NAME);
            System.out.println("---" + new QName(def.getTargetNamespace(), n.getNodeValue()));
            bindingMap.put(new QName(n.getNamespaceURI(), n.getLocalName()), new java.util.ArrayList());
            Node nn = node.getAttributes().getNamedItem("type");
            System.out.println(nn.getNodeValue());
            
        }*/

    }

}
