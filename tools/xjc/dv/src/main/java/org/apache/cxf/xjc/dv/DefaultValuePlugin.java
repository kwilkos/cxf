package org.apache.cxf.xjc.dv;

import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.NamespaceContextAdapter;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XmlString;

/**
 * Modifies the JAXB code model to initialise fields mapped from schema elements 
 * with their default value.
 */
public class DefaultValuePlugin extends Plugin {
    
    public DefaultValuePlugin() {
    }

    public String getOptionName() {
        return "Xdv";
    }

    public String getUsage() {
        return "-Xdv: Initialize fields mapped from elements with their default values";
    }

    public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) {
        System.out.println("Running default value plugin.");
        for (ClassOutline co : outline.getClasses()) {
            for (FieldOutline f : co.getDeclaredFields()) {

                // Use XML schema object model to determine if field is mapped
                // from an element (attributes default values are handled
                // natively) and get its default value.

                String fieldName = f.getPropertyInfo().getName(false);
                XmlString xmlDefaultValue = null;
                XSType xsType = null;

                if (f.getPropertyInfo().getSchemaComponent() instanceof XSParticle) {
                    XSParticle particle = (XSParticle)f.getPropertyInfo().getSchemaComponent();
                    XSTerm term = particle.getTerm();
                    XSElementDecl element = null;

                    if (term.isElementDecl()) {
                        element = particle.getTerm().asElementDecl();
                        xmlDefaultValue = element.getDefaultValue();                        
                        xsType = element.getType();
                    }
                }

                if (null == xmlDefaultValue) {
                    continue;
                }
                
                String defaultValue = xmlDefaultValue.value;
                
                if (null == defaultValue) {
                    continue;
                }

                JType type = f.getRawType();
                String typeName = type.fullName();                

                JDefinedClass impl = co.implClass;
                Map<String, JFieldVar> fields = impl.fields();
                JFieldVar var = fields.get(fieldName);
 
                if ("java.lang.Boolean".equals(typeName)) {
                    var.init(JExpr.direct(Boolean.valueOf(defaultValue) ? "Boolean.TRUE" : "Boolean.FALSE"));
                } else if ("java.lang.Byte".equals(typeName)) {
                    var.init(JExpr._new(type)
                        .arg(JExpr.cast(type.unboxify(), 
                            JExpr.lit(new Byte(Short.valueOf(defaultValue).byteValue())))));
                } else if ("java.lang.Double".equals(typeName)) {
                    var.init(JExpr._new(type)
                        .arg(JExpr.lit(new Double(Double.valueOf(defaultValue).doubleValue()))));
                } else if ("java.lang.Float".equals(typeName)) {
                    var.init(JExpr._new(type)
                             .arg(JExpr.lit(new Float(Float.valueOf(defaultValue).floatValue()))));
                } else if ("java.lang.Integer".equals(typeName)) {
                    var.init(JExpr._new(type)
                        .arg(JExpr.lit(new Integer(Integer.valueOf(defaultValue).intValue()))));
                } else if ("java.lang.Long".equals(typeName)) {
                    var.init(JExpr._new(type)
                        .arg(JExpr.lit(new Long(Long.valueOf(defaultValue).longValue()))));
                } else if ("java.lang.Short".equals(typeName)) {
                    var.init(JExpr._new(type)
                        .arg(JExpr.cast(type.unboxify(), 
                            JExpr.lit(new Short(Short.valueOf(defaultValue).shortValue())))));
                } else if ("java.lang.String".equals(type.fullName())) {
                    var.init(JExpr.lit(defaultValue));
                } else if ("java.math.BigInteger".equals(type.fullName())) {
                    var.init(JExpr._new(type).arg(JExpr.lit(defaultValue)));
                } else if ("java.math.BigDecimal".equals(type.fullName())) {
                    var.init(JExpr._new(type).arg(JExpr.lit(defaultValue)));
                } else if ("byte[]".equals(type.fullName()) && xsType.isSimpleType()) {
                    while (!"anySimpleType".equals(xsType.getBaseType().getName())) {
                        xsType = xsType.getBaseType();
                    }
                    if ("base64Binary".equals(xsType.getName())) {
                       // var.init(outline.getCodeModel().ref(DatatypeConverter.class)
                       //          .staticInvoke("parseBase64Binary").arg(defaultValue));
                    } else if ("hexBinary".equals(xsType.getName())) {
                        // var.init(JExpr._new(outline.getCodeModel().ref(HexBinaryAdapter.class))
                        //    .invoke("unmarshal").arg(defaultValue));
                    }
                } else if ("javax.xml.namespace.QName".equals(typeName)) {
                    NamespaceContext nsc = new NamespaceContextAdapter(xmlDefaultValue);
                    QName qn = DatatypeConverter.parseQName(xmlDefaultValue.value, nsc);
                    var.init(JExpr._new(outline.getCodeModel().ref(QName.class))
                        .arg(qn.getNamespaceURI())
                        .arg(qn.getLocalPart())
                        .arg(qn.getPrefix()));
                }
                // TODO: GregorianCalendar, ...
            }
        }

        return true;
    }
}
