package org.objectweb.celtix.tools.utils;

import org.w3c.dom.Element;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;

import org.objectweb.celtix.tools.common.WSDLConstants;

public class LineNumDOMParser extends DOMParser {
    private XMLLocator locator;

    public LineNumDOMParser() throws SAXNotSupportedException, SAXNotRecognizedException {
        super();

        setFeature(DEFER_NODE_EXPANSION, false);

    }

    public LineNumDOMParser(XMLParserConfiguration arg0) {
        super(arg0);
        try {
            setFeature(DEFER_NODE_EXPANSION, false);
        } catch (SAXNotSupportedException e) {
            System.out.println(e);
        } catch (SAXNotRecognizedException e) {
            System.out.println(e);
        }
    }

    public LineNumDOMParser(SymbolTable st) {
        super(st);
    }

    public LineNumDOMParser(SymbolTable st, XMLGrammarPool xp) {
        super(st, xp);
    }

    public void startElement(QName qn, XMLAttributes xmlAtr, Augmentations aug) throws XNIException {
        Element element;
        /*    String ns = qn.uri;
        
         * if (ns != null && (ns.equals(Constants.NS_URI_XSD_2001) ||
         * ns.equals(Constants.NS_URI_XSD_1999) || ns
         * .equals(Constants.NS_URI_XSD_2000))) { int numatts =
         * xmlAtr.getLength(); System.out.println("---arg1--- " + xmlAtr); for
         * (int i = 0; i < numatts; i++) { String nonNormalizedValue =
         * xmlAtr.getNonNormalizedValue(i);
         * System.out.println("---nonNormalizedValue--- " + nonNormalizedValue);
         * xmlAtr.setValue(i, nonNormalizedValue); } }
         */
        super.startElement(qn, xmlAtr, aug);
        try {
            element = (Element)getProperty(CURRENT_ELEMENT_NODE);
            element.setUserData(WSDLConstants.NODE_LOCATION, new ElementLocator(locator.getLineNumber(),
                                                                                locator.getColumnNumber()),
                                null);

        } catch (ClassCastException e) {
            // System.out.println(e);
        } catch (SAXNotRecognizedException e) {
            // System.out.println(e);
        } catch (SAXNotSupportedException e) {
            // System.out.println(e);
        }

    }

    public void startDocument(XMLLocator xmlLoc, String str, NamespaceContext nsCxt, Augmentations aug)
        throws XNIException {
        locator = xmlLoc;
        super.startDocument(xmlLoc, str, nsCxt, aug);

    }

}
