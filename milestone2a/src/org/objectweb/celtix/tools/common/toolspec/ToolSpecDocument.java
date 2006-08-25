package org.objectweb.celtix.tools.common.toolspec;


/**
 * This class represents an XML toolspec document.
 *
 */
public class ToolSpecDocument {

    private final ToolSpecDeclaration toolSpecDecl;

    public ToolSpecDocument(ToolSpecDeclaration ts) {
        this.toolSpecDecl = ts;
    }

    public ToolSpecDeclaration getToolSpecDeclaration() {
        return toolSpecDecl;
    }

}
