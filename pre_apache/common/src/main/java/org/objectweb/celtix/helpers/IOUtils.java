package org.objectweb.celtix.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IOUtils {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    
    private IOUtils() {
        
    }
    
    public static void copy(final InputStream input, final OutputStream output)
        throws IOException {
        copy(input, output, DEFAULT_BUFFER_SIZE);
    }

    
    public static void copy(final InputStream input,
                            final OutputStream output,
                            final int bufferSize)
        throws IOException {
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        n = input.read(buffer);
        while (-1 != n) {
            output.write(buffer, 0, n);
            n = input.read(buffer);
        }
    }
}
