package org.apache.cxf.aegis.type.basic;

import java.sql.Time;
import java.text.ParseException;
import java.util.Calendar;

import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.util.date.XsTimeFormat;
import org.apache.cxf.aegis.xml.MessageReader;
import org.apache.cxf.aegis.xml.MessageWriter;

/**
 * Type for the Time class which serializes to an xs:time.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public class TimeType extends Type {
    private static XsTimeFormat format = new XsTimeFormat();

    @Override
    public Object readObject(MessageReader reader, Context context) throws DatabindingException {
        String value = reader.getValue();

        if (value == null) {
            return null;
        }

        try {
            Calendar c = (Calendar)format.parseObject(value);
            return new Time(c.getTimeInMillis());
        } catch (ParseException e) {
            throw new DatabindingException("Could not parse xs:dateTime: " + e.getMessage(), e);
        }
    }

    @Override
    public void writeObject(Object object, MessageWriter writer, Context context) {
        Calendar c = Calendar.getInstance();
        c.setTime((Time)object);
        writer.writeValue(format.format(c));
    }
}
