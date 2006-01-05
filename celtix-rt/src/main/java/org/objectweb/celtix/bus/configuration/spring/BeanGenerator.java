package org.objectweb.celtix.bus.configuration.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.xml.sax.InputSource;

import org.objectweb.celtix.bus.configuration.ConfigurationMetadataBuilder;
import org.objectweb.celtix.bus.configuration.TypeSchema;
import org.objectweb.celtix.bus.configuration.TypeSchemaHelper;
import org.objectweb.celtix.bus.jaxb.JAXBUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationMetadata;

public class BeanGenerator {

    private static final Logger LOG = LogUtils.getL7dLogger(BeanGenerator.class);
    
    File outputDir;
    TypeSchemaHelper tsh = new TypeSchemaHelper();

    public BeanGenerator() {
        outputDir = new File(".");
    }
 
    public static void main(String[] args) {
        
        BeanGenerator generator = new BeanGenerator();
        List<String> schemaFiles = new ArrayList<String>();
        int i = 0;
        while (i < args.length) {
            if ("-d".equals(args[i]) && i < (args.length - 1)) {
                i++; 
                generator.setOutputDir(args[i]);
            } else {
                schemaFiles.addAll(splitArgument(args[i]));
            }
            i++;
        }
        for (String sf : schemaFiles) {
            generator.generateBean(sf);
        }
    }
    
    void setOutputDir(String dir) {
        outputDir = new File(dir);
    }

    void generateBean(String path) {
        System.out.println("Generating bean from resource : " + path);
        InputSource src = null;
        try {
            src = new InputSource(new FileInputStream(path));
        } catch (IOException ex) {
            throw new ConfigurationException(new Message("FAILED_TO_GENERATE_BEAN_EXC", LOG), ex);
        }
        
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder();
        builder.setValidation(true);
        ConfigurationMetadata model = null;

        try {
            model = builder.build(src);
        } catch (IOException ex) {
            throw new ConfigurationException(new Message("FAILED_TO_GENERATE_BEAN_EXC", LOG), ex);
        }
        
        String className = SpringUtils.getBeanClassName(model.getNamespaceURI());
        
        StringBuffer classFileName = new StringBuffer(className);
        for (int i = 0; i < classFileName.length(); i++) {
            if ('.' == classFileName.charAt(i)) {
                classFileName.setCharAt(i, File.separatorChar);
            }
        }
        classFileName.append(".java");        
        
        File classFile = new File(outputDir, classFileName.toString());
        File dir = classFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        LOG.info("Generating class: " + className + "\n"
            +    "           file:  " + classFile.getPath()); 
        
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(classFile));
            // pw = new PrintWriter(System.out);
            writeClass(pw, model, className);
        } catch (IOException ex) {
            throw new ConfigurationException(new Message("FAILED_TO_GENERATE_BEAN_EXC", LOG), ex);          
        } finally {
            pw.close();
        }
    }
    
    void writeClass(PrintWriter pw, ConfigurationMetadata model, String qualifiedClassName) {
        
        int index = qualifiedClassName.lastIndexOf('.');
        
        String packageName = qualifiedClassName.substring(0, index);
        String className = qualifiedClassName.substring(index + 1);
        pw.print("package ");
        pw.print(packageName);
        pw.println(";");            
        pw.println();
        
        writeImports(pw, model);
        
        pw.print("public class ");
        pw.print(className);
        pw.println(" {");
        pw.println();
        
        writeDataMembers(pw, model);
        writeAccessors(pw, model); 
        pw.println("}");
    }
    
    private void writeImports(PrintWriter pw, ConfigurationMetadata model) {
        Collection<String> classNames = new ArrayList<String>();
        
        for (ConfigurationItemMetadata definition : model.getDefinitions()) {
            QName type = definition.getType(); 
            String className = getClassName(type, true);
            int index = className.lastIndexOf('.');
            if (index < 0 || "java.lang".equals(className.substring(0, index))) {
                continue;
            }
            if (!classNames.contains(className)) {
                classNames.add(className);
            }
        }
        
        if (!classNames.contains("java.util.Collection")) {
            classNames.add("java.util.Collection");
        }
        
        if (!classNames.contains("java.util.ArrayList")) {
            classNames.add("java.util.ArrayList");
        }
        
        for (String className : classNames) {
            pw.print("import ");
            pw.print(className);
            pw.println(";");
        }
        pw.println();
    }
    
    private void writeDataMembers(PrintWriter pw, ConfigurationMetadata model) {
   
        for (ConfigurationItemMetadata definition : model.getDefinitions()) {
            QName type = definition.getType();            
            String className = getClassName(type, false);
            String memberName = JAXBUtils.nameToIdentifier(definition.getName(), 
                                                           JAXBUtils.IdentifierType.VARIABLE);
            pw.print("    private ");
            pw.print(className);
            pw.print(" ");
            pw.print(memberName);
            pw.println(";");           
        }
        pw.println();
        pw.print("    private Collection<String> _initialized = ");
        pw.println("new ArrayList<String>();");
        
        pw.println();
    }
    
    private void writeAccessors(PrintWriter pw, ConfigurationMetadata model) {
        
        for (ConfigurationItemMetadata definition : model.getDefinitions()) {
            QName type = definition.getType();            
            String className = getClassName(type, false);
            String memberName = JAXBUtils.nameToIdentifier(definition.getName(), 
                                                           JAXBUtils.IdentifierType.VARIABLE);
            
            pw.print("    public ");
            pw.print(className);
            pw.print(" ");
            pw.print(JAXBUtils.nameToIdentifier(definition.getName(), JAXBUtils.IdentifierType.GETTER));
            pw.println("() {");
            pw.print("        return ");
            pw.print(memberName);
            pw.println(";");
            pw.println("    }");
            pw.println();
            
            pw.print("    public void ");
            pw.print(JAXBUtils.nameToIdentifier(definition.getName(), JAXBUtils.IdentifierType.SETTER));
            pw.print("(");
            pw.print(className);
            pw.println(" obj) {");
            pw.print("        ");
            pw.print(memberName);
            pw.println(" = obj;");
            pw.print("        if (!_initialized.contains(\"");
            pw.print(definition.getName());
            pw.println("\")) {");
            pw.print("            _initialized.add(\"");
            pw.print(definition.getName());
            pw.println("\");");
            pw.println("        }");
            pw.println("    }");
            pw.println();
        }
        
        pw.println("    public boolean isSet(String name) {");
        pw.println("        return _initialized.contains(name);");
        pw.println("    }");
    }
    
    protected static String getClassName(QName typeName, boolean qualified) { 
        String baseType = null;
        TypeSchema ts = null;
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(typeName.getNamespaceURI())) {
            baseType = typeName.getLocalPart(); 
        } else {
            ts = new TypeSchemaHelper().get(typeName.getNamespaceURI());
            baseType = ts.getXMLSchemaBaseType(typeName.getLocalPart());
        }
        String className = null;
        if (null != baseType) {
            className = JAXBUtils.builtInTypeToJavaType(baseType);
            if (className != null && !qualified) {
                int index = className.lastIndexOf('.');
                if (index >= 0) {
                    className = className.substring(index + 1);
                }
            }
        }
        if (null == className) {
            baseType = typeName.getLocalPart();
            className = JAXBUtils.nameToIdentifier(baseType,
                                                   JAXBUtils.IdentifierType.CLASS);
            if (qualified) {
                className = ts.getPackageName() + "." + className;
            }
        }
        return className;
    }

    private static List<String> splitArgument(String arg) {
        List<String> filenames  = new ArrayList<String>();

        String filename = null;
        int from = 0;
        while (from < arg.length()) {
            int to = 0;
            if (arg.indexOf("\"", from)  == from) {
                to = arg.indexOf("\"", from + 1);
                if (to >= 0) {
                    filename = arg.substring(from + 1, to);
                    to++;
                } else {
                    throw new IllegalArgumentException(new Message("MISMATCHED_QUOTES_EXC", LOG).toString());
                }
            } else {
                to = from;
                while (to < arg.length() && !Character.isWhitespace(arg.charAt(to))) {
                    to++;
                }
                filename = arg.substring(from, to);
            }
            while (to < arg.length() && Character.isWhitespace(arg.charAt(to))) {
                to++;
            }
            from = to;
            filenames.add(filename);
        }
        return filenames;
    }
}
