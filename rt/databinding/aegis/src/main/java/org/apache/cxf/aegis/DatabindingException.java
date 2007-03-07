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
package org.apache.cxf.aegis;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * 
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Feb 14, 2004
 */
public class DatabindingException extends RuntimeException {
    private final Throwable cause;
    private String message2;

    /**
     * Constructs a new exception with the specified detail
     * message.
     * 
     * @param message the detail message.
     */
    public DatabindingException(String message) {
        super();
        this.message2 = message;
        this.cause = null;
    }

    /**
     * Constructs a new exception with the specified detail
     * message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public DatabindingException(String message, Throwable cause) {
        super(message);
        this.message2 = message;
        this.cause = cause;
    }

    /**
     * Returns the cause of this throwable or <code>null</code> if the cause
     * is nonexistent or unknown.
     * 
     * @return the nested cause.
     */
    @Override
    public Throwable getCause() {
        return this.cause == this ? null : this.cause;
    }

    /**
     * Return the detail message, including the message from the
     * {@link #getCause() nested exception} if there is one.
     * 
     * @return the detail message.
     */
    @Override
    public String getMessage() {
        if (this.cause == null || this.cause == this) {
            return message2;
        } else {
            return message2 + ". Nested exception is " + this.cause.getClass().getName() + ": "
                   + this.cause.getMessage();
        }
    }

    public String getActualMessage() {
        return message2;
    }

    /**
     * Prints this throwable and its backtrace to the specified print stream.
     * 
     * @param s <code>PrintStream</code> to use for output
     */
    @Override
    public void printStackTrace(PrintStream s) {
        if (this.cause == null || this.cause == this) {
            super.printStackTrace(s);
        } else {
            s.println(this);
            this.cause.printStackTrace(s);
        }
    }

    /**
     * Prints this throwable and its backtrace to the specified print writer.
     * 
     * @param w <code>PrintWriter</code> to use for output
     */
    @Override
    public void printStackTrace(PrintWriter w) {
        if (this.cause == null || this.cause == this) {
            super.printStackTrace(w);
        } else {
            w.println(this);
            this.cause.printStackTrace(w);
        }
    }

    public void prepend(String m) {
        if (this.message2 != null) {
            this.message2 = m + ": " + this.message2;
        } else {
            this.message2 = m;
        }
    }

    public void setMessage(String s) {
        message2 = s;
    }
}
