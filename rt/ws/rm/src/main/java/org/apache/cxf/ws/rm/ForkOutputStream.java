/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.ws.rm;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream that multicasts its data to several underlying output streams.
 */
public class ForkOutputStream extends OutputStream {

    final OutputStream[] outputStreams;
    
    public ForkOutputStream(OutputStream... streams) {
        outputStreams = streams;
    }
    
    @Override
    public void close() throws IOException {
        for (OutputStream o : outputStreams) {
            if (o != System.out) {
                o.close();
            } else {
                System.out.println();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        for (OutputStream o : outputStreams) {
            o.flush();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream o : outputStreams) {
            o.write(b, off, len);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (OutputStream o : outputStreams) {
            o.write(b);
        }
    }

    @Override
    public void write(int b) throws IOException {
        for (OutputStream o : outputStreams) {
            o.write(b);
        }
        
    }

}
