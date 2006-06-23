package org.objectweb.celtix.tools.wsdl2java.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.util.ClassCollector;
import org.objectweb.celtix.tools.util.FileWriterUtil;
import org.objectweb.celtix.version.Version;

public abstract class AbstractGenerator {
   
    public static final String TEMPLATE_BASE = "org/objectweb/celtix/tools/wsdl2java/generator/template";
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractGenerator.class);
    protected ProcessorEnvironment env;
    protected JavaModel javaModel;
    protected Map<String, Object> attributes = new HashMap<String, Object>();
    protected String name;
    protected ClassCollector collector;
    public  AbstractGenerator() {
        
    }
    public AbstractGenerator(JavaModel jmodel, ProcessorEnvironment penv) {
        javaModel = jmodel;
        this.env = penv;
        collector = (ClassCollector)env.get(ToolConstants.GENERATED_CLASS_COLLECTOR);
    }

    public abstract boolean passthrough();

    public abstract void generate() throws ToolException;

    protected void doWrite(String templateName, Writer outputs) throws ToolException {
        Template tmpl = null;
        try {
            tmpl = Velocity.getTemplate(templateName);
        } catch (Exception e) {
            Message msg = new Message("TEMPLATE_MISSING", LOG, templateName);
            throw new ToolException(msg, e);
        }

        VelocityContext ctx = new VelocityContext();

        for (Iterator iter = attributes.keySet().iterator(); iter.hasNext();) {
            String key = (String)iter.next();
            ctx.put(key, attributes.get(key));
        }

        VelocityWriter writer = new VelocityWriter(outputs);
        try {
            tmpl.merge(ctx, writer);
            writer.close();
        } catch (Exception e) {
            Message msg = new Message("VELOCITY_ENGINE_WRITE_ERRORS", LOG);
            throw new ToolException(msg, e);
        }
    }

    protected boolean isCollision(String packageName, String filename) throws ToolException {
        return isCollision(packageName, filename, ".java");
    }

    protected boolean isCollision(String packageName, String filename, String ext) throws ToolException {
        FileWriterUtil fw = new FileWriterUtil((String)env.get(ToolConstants.CFG_OUTPUTDIR));
        return fw.isCollision(packageName, filename + ext);
    }

    protected Writer parseOutputName(String packageName, String filename, String ext) throws ToolException {
        FileWriterUtil fw = null;
        Writer writer = null;

        fw = new FileWriterUtil((String)env.get(ToolConstants.CFG_OUTPUTDIR));
        try {
            writer = fw.getWriter(packageName, filename + ext);
        } catch (IOException ioe) {
            Message msg = new Message("FAIL_TO_WRITE_FILE", LOG, packageName + "." + filename + ext);
            throw new ToolException(msg, ioe);
        }

        return writer;
    }

    protected Writer parseOutputName(String packageName, String filename) throws ToolException {
        // collector.
        if (ToolConstants.CLT_GENERATOR.equals(name)) {
            collector.addClientClassName(packageName , filename , packageName + "." + filename);
        }
        
        if (ToolConstants.FAULT_GENERATOR.equals(name)) {
            collector.addExceptionClassName(packageName , filename , packageName + "." + filename);
        }
        if (ToolConstants.SERVICE_GENERATOR.equals(name)) {
            collector.addServiceClassName(packageName , filename , packageName + "." + filename);
        }
        if (ToolConstants.SVR_GENERATOR.equals(name)) {
            collector.addServiceClassName(packageName , filename , packageName + "." + filename);
            
        }
        
        
        return parseOutputName(packageName, filename, ".java");
    }

    protected void setAttributes(String n, Object value) {
        attributes.put(n, value);
    }

    protected void setCommonAttributes() {
        attributes.put("currentdate", Calendar.getInstance().getTime());
        attributes.put("version", Version.getCurrentVersion());
    }

    protected void clearAttributes() {
        attributes.clear();
    }

    public void setEnvironment(ProcessorEnvironment penv) {
        this.env = penv;

    }

    public ProcessorEnvironment getEnvironment() {
        return this.env;
    }

    public String getName() {
        return this.name;
    }
}
