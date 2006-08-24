package org.apache.cxf.tools.common.toolspec.parser;

import org.w3c.dom.Element;

public interface TokenConsumer {
    boolean accept(TokenInputStream args, Element result, ErrorVisitor errors);
    boolean isSatisfied(ErrorVisitor errors);
}
