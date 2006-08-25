package org.objectweb.celtix.tools.common.model;

import java.lang.reflect.Constructor;
import java.util.*;
import javax.xml.namespace.QName;
import com.sun.xml.bind.api.TypeReference;

public class JavaType {
    
    public static enum Style { IN, OUT, INOUT }
    private static Map<String, String> typeMapping = new HashMap<String, String>();

    static {
        typeMapping.put("boolean", "false");
        typeMapping.put("int", "0");
        typeMapping.put("long", "0");
        typeMapping.put("short", "Short.parseShort(\"0\")");
        typeMapping.put("byte", "Byte.parseByte(\"0\")");
        typeMapping.put("float", "0.0f");
        typeMapping.put("double", "0.0");
        typeMapping.put("char", "0");
        
        typeMapping.put("javax.xml.namespace.QName", "new javax.xml.namespace.QName(\"\", \"\")");
        typeMapping.put("java.net.URI", "new java.net.URI(\"\")");

        typeMapping.put("java.math.BigInteger", "new java.math.BigInteger(\"0\")");
        typeMapping.put("java.math.BigDecimal", "new java.math.BigDecimal(\"0\")");
        typeMapping.put("javax.xml.datatype.XMLGregorianCalendar", "null");
        // javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar()
        typeMapping.put("javax.xml.datatype.Duration", "null");
        // javax.xml.datatype.DatatypeFactory.newInstance().newDuration(\"P1Y35DT60M60.500S\")
    }

    protected String name;
    protected String type;
    protected String className;
    protected String targetNamespace;
    protected Style style;
    protected TypeReference typeRef;
    protected boolean isHeader;
    private QName qname;

    public JavaType() {
    }

    public JavaType(String n, String t, String tns) {
        this.name = n;
        this.type = t;
        this.targetNamespace = tns;
        this.className = t;
    }

    public void setQName(QName qn) {
        this.qname = qn;
    }

    public QName getQName() {
        return this.qname;
    }

    public void setClassName(String clzName) {
        this.className = clzName;
    }

    public String getClassName() {
        return this.className;
    }

    public String getDefaultTypeValue() {
        if (this.className.trim().endsWith("[]")) {
            return "new " + this.className.substring(0, this.className.length() - 2) + "[0]";
        }
        if (typeMapping.containsKey(this.className.trim())) {
            return typeMapping.get(this.className);
        }

        try {
            if (hasDefaultConstructor(Class.forName(this.className))) {
                return "new " + this.className + "()";
            }
        } catch (ClassNotFoundException e) {
            // DONE
        }
        return "null";
    }

    private boolean hasDefaultConstructor(Class clz) {
        Constructor[] cons = clz.getConstructors();
        if (cons.length == 0) {
            return false;
        } else {
            for (int i = 0; i < cons.length; i++) {
                if (cons[i].getParameterTypes().length == 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void setTargetNamespace(String tns) {
        this.targetNamespace = tns;
    }

    public String getTargetNamespace() {
        return this.targetNamespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public String getType() {
        return type;
    }

    public void setType(String t) {
        type = t;
    }
    
    //
    // getter and setter for in, out inout style
    //
    public JavaType.Style getStyle() {
        return this.style;
    }

    public void setStyle(Style s) {
        this.style = s;
    }

    public boolean isIN() {
        return this.style == Style.IN;
    }

    public boolean isOUT() {
        return this.style == Style.OUT;
    }

    public boolean isINOUT() {
        return this.style == Style.INOUT;
    }
    public void setTypeReference(TypeReference ref) {
        this.typeRef = ref;
    }

    public TypeReference getTypeReference() {
        return this.typeRef;
    }

    public void setHeader(boolean header) {
        this.isHeader = header;
    }

    public boolean isHeader() {
        return this.isHeader;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("\nName: ");
        sb.append(this.name);
        sb.append("\nType: ");
        sb.append(this.type);
        sb.append("\nClass Name: ");
        sb.append(this.className);
        sb.append("\nTargetNamespace: ");
        sb.append(this.targetNamespace);
        sb.append("\nStyle: ");
        sb.append(style);
        return sb.toString();
    }
}
