package org.objectweb.celtix.tools.generators;

import java.io.*;
import java.util.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import org.objectweb.celtix.tools.Version;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.utils.FileWriterUtil;

public abstract class AbstractGenerator {

    protected static final String TEMPLATE_BASE = "org/objectweb/celtix/tools/generators/wsdl2/templates";
    protected ProcessorEnvironment env;
    protected Map<String, Object> attributes = new HashMap<String, Object>();
    protected String name;

    public abstract boolean passthrough();
    public abstract void generate() throws Exception;

    protected void doWrite(String templateName, Writer outputs) throws Exception {
        Template tmpl = Velocity.getTemplate(templateName);

        VelocityContext ctx = new VelocityContext();
        
        for (Iterator iter = attributes.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            ctx.put(key, attributes.get(key));
        }

        BufferedWriter writer = new BufferedWriter(outputs);
        tmpl.merge(ctx, writer);
        writer.close();
    }

    protected Writer parseOutputName(String packageName, String filename) throws Exception {
        FileWriterUtil fw = new FileWriterUtil((String) env.get(ToolConstants.CFG_OUTPUTDIR));
        return fw.getWriter(packageName, filename + ".java");
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
