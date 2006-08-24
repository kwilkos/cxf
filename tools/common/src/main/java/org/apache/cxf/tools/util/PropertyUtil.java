package org.apache.cxf.tools.util;

import java.io.*;
import java.util.*;
import org.apache.cxf.common.util.StringUtils;

public class PropertyUtil {
    private static final String DEFAULT_DELIM = "=";
    private Map<String, String>  maps = new HashMap<String, String>();

    public void load(InputStream is, String delim) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (!StringUtils.isEmpty(line)) {
            StringTokenizer st = new StringTokenizer(line, delim);
            String key = null;
            String value = null;
            if (st.hasMoreTokens()) {
                key  = st.nextToken().trim();
            }
            if (st.hasMoreTokens()) {
                value = st.nextToken().trim();
            }

            maps.put(key, value);

            line = br.readLine();
        }
        br.close();
    }
    
    public void load(InputStream is) throws IOException {
        load(is, DEFAULT_DELIM);
    }
    
    public String getProperty(String key) {
        return this.maps.get(key);
    }

    public Map<String, String> getMaps() {
        return this.maps;
    }
}
