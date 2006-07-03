package org.objectweb.celtix.bindings.soap2.attachments.types;


import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * org.objectweb.celtix.bindings.soap2.attachments.types package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private static final QName DETAIL_QNAME = new QName(
            "http://celtix.objectweb.org/bindings/soap2/attachments/types",
            "Detail");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package:
     * org.objectweb.celtix.bindings.soap2.attachments.types
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DetailType }
     */
    public DetailType createDetailType() {
        return new DetailType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DetailType }{@code >}}
     */
    @XmlElementDecl(namespace = "http://celtix.objectweb.org/bindings/soap2/attachments/types", 
                    name = "Detail")
    public JAXBElement<DetailType> createDetail(DetailType value) {
        return new JAXBElement<DetailType>(DETAIL_QNAME, DetailType.class, null, value);
    }

}
