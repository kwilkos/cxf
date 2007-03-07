package org.apache.cxf.aegis.type.basic;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.util.date.XsDateFormat;
import org.apache.cxf.aegis.xml.MessageReader;
import org.apache.cxf.aegis.xml.MessageWriter;

/**
 * Type for the Date class which serializes as an xsd:date (no time
 * information).
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public class DateType extends Type {
    private static XsDateFormat format = new XsDateFormat();

    @Override
    public Object readObject(MessageReader reader, Context context) throws DatabindingException {
        String value = reader.getValue();

        if (value == null) {
            return null;
        }

        try {
            Calendar c = (Calendar)format.parseObject(value);
            return c.getTime();
        } catch (ParseException e) {
            throw new DatabindingException("Could not parse xs:dat: " + e.getMessage(), e);
        }
    }

    @Override
    public void writeObject(Object object, MessageWriter writer, Context context) {
        Calendar c = Calendar.getInstance();
        c.setTime((Date)object);
        writer.writeValue(format.format(c));
    }
}
