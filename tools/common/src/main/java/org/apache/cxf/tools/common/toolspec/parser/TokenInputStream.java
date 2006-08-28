package org.apache.cxf.tools.common.toolspec.parser;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;

public class TokenInputStream {

    private static final Logger LOG = LogUtils.getL7dLogger(TokenInputStream.class);
    private final String[] tokens;
    private int pos;

    public TokenInputStream(String[] t) {
        this.tokens = t;
    }

    public String read() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Reading token " + tokens[pos]);
        }
        return tokens[pos++];
    }

    public String readNext() {
        return read(pos++);
    }

    public String read(int position) {
        if (position < 0) {
            pos = 0;
        }
        if (position > tokens.length) {
            pos = tokens.length - 1;
        }
        return tokens[pos];
    }

    public String readPre() {
        if (pos != 0) {
            pos--;
        }
        return tokens[pos];
    }

    public String peek() {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Peeking token " + tokens[pos]);
        }
        return tokens[pos];
    }

    public String peekPre() {
        if (pos == 0) {
            return tokens[pos];
        }
        return tokens[pos - 1];
    }

    public String peek(int position) {
        if (position < 0) {
            return tokens[0];
        }
        if (position > tokens.length) {
            return tokens[tokens.length - 1];
        }
        return tokens[position];
    }

    public int getPosition() {
        return pos;
    }

    public void setPosition(int p) {
        this.pos = p;
    }

    public int available() {
        return tokens.length - pos;
    }

    public boolean hasNext() {
        return available() > 1;
    }

    public boolean isOutOfBound() {
        return pos >= tokens.length;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("[ ");

        for (int i = pos; i < tokens.length; i++) {
            sb.append(tokens[i]);
            if (i < tokens.length - 1) {
                sb.append(" ");
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

}
