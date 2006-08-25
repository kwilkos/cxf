package org.objectweb.celtix.bus.configuration.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.xml.sax.InputSource;

import org.objectweb.celtix.bus.configuration.ConfigurationMetadataBuilder;
import org.objectweb.celtix.bus.configuration.ConfigurationMetadataImpl;
import org.objectweb.celtix.bus.configuration.TypeSchema;
import org.objectweb.celtix.bus.jaxb.JAXBUtils;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationMetadata;

public class BeanGenerator {

    private static final Logger LOG = LogUtils.getL7dLogger(BeanGenerator.class);
    
    File outputDir;

    protected BeanGenerator() {
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
                schemaFiles.add(args[i]);
            }
            i++;
        }
        for (String sf : schemaFiles) {
            if (!generator.generateBean(sf)) {
                System.err.println("Failed to generate bean for: " + sf);
                System.exit(1);
            }
        }
    }
    
    public void setOutputDir(String path) {
        outputDir = new File(path);     
    }

    public boolean generateBean(String path) {
        
        InputSource src = null;
        boolean result = false;
        try {
            src = new InputSource(new FileInputStream(path));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "FAILED_TO_GENERATE_BEAN_MSG", ex);
            return result;
        }
        
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder();
        ConfigurationMetadata model = null;

        try {
            model = builder.build(src);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "FAILED_TO_GENERATE_BEAN_MSG", ex);
            return result;
        }
              
        String namespaceURI = model.getNamespaceURI();
        
        StringBuffer packageName = new StringBuffer(JAXBUtils.namespaceURIToPackage(namespaceURI));
        int index = packageName.lastIndexOf(".");
        StringBuffer className = new StringBuffer();
        if (index >= 0) {
            className.append(packageName.substring(index + 1));
        } else {
            className.append(packageName);
        }
        if (Character.isLowerCase(className.charAt(0))) {
            className.setCharAt(0, Character.toUpperCase(className.charAt(0)));
        }
        
        packageName .append(".spring");
        className.append("Bean");
        
        StringBuffer classFileName = new StringBuffer(packageName);
        for (int i = 0; i < classFileName.length(); i++) {
            if ('.' == classFileName.charAt(i)) {
                classFileName.setCharAt(i, File.separatorChar);
            }
        }
        classFileName.append(File.separatorChar);
        classFileName.append(className);
        classFileName.append(".java");        
        
        File classFile = new File(outputDir, classFileName.toString());
        File dir = classFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        LOG.info("Generating class: " + className.toString() + "\n"
            +    "           file:  " + classFile.getPath()); 
        
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(classFile));
            writeClass(pw, model, packageName.toString(), className.toString());
            result = true;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "FAILED_TO_GENERATE_BEAN", ex);
            
        } finally {
            pw.close();
        }
        
        return result;

    }
    
    void writeClass(PrintWriter pw, ConfigurationMetadata model, String packageName, String className) {
        
        pw.print("package ");
        pw.print(packageName.toString());
        pw.println(";");            
        pw.println();
        
        writeImports(pw, model);
        
        pw.print("public class ");
        pw.print(className.toString());
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
            TypeSchema ts = ((ConfigurationMetadataImpl)model).getTypeSchema(type.getNamespaceURI());
            String typeName = ts.getTypeType(type.getLocalPart());
            String className = JAXBUtils.nameToIdentifier(typeName,
                                                   JAXBUtils.IdentifierType.CLASS);
            String qualifiedClassName = ts.getPackageName() + "." + className;
            if (!classNames.contains(qualifiedClassName)) {
                classNames.add(qualifiedClassName);
            }
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
            TypeSchema ts = ((ConfigurationMetadataImpl)model).getTypeSchema(type.getNamespaceURI());
            String typeName = ts.getTypeType(type.getLocalPart());
            String className = JAXBUtils.nameToIdentifier(typeName,
                                                   JAXBUtils.IdentifierType.CLASS);
            
            pw.print("    private ");
            pw.print(className);
            pw.print(" ");
            pw.print(definition.getName());
            pw.println(";");            
        }
        pw.println();
    }
    
    private void writeAccessors(PrintWriter pw, ConfigurationMetadata model) {
        for (ConfigurationItemMetadata definition : model.getDefinitions()) {
            QName type = definition.getType();            
            TypeSchema ts = ((ConfigurationMetadataImpl)model).getTypeSchema(type.getNamespaceURI());
            String typeName = ts.getTypeType(type.getLocalPart());
            String className = JAXBUtils.nameToIdentifier(typeName,
                                                   JAXBUtils.IdentifierType.CLASS);
            pw.print("    public ");
            pw.print(className);
            pw.print(" ");
            pw.print(SpringUtils.getGetterName(definition));
            pw.println("() {");
            pw.print("        return ");
            pw.print(SpringUtils.getMemberName(definition));
            pw.println(";");
            pw.println("    }");
            pw.println();
            
            pw.print("    public void ");
            pw.print(SpringUtils.getSetterName(definition));
            pw.print("(");
            pw.print(className);
            pw.println(" obj) {");
            pw.print("        ");
            pw.print(SpringUtils.getMemberName(definition));
            pw.println(" = obj;");
            pw.println("    }");
            pw.println();
        }
    }
}
