package org.objectweb.celtix.tools.common.dom;

import junit.framework.TestCase;

public class JavaResourceEntityResolverTest extends TestCase {
    public void testMassMethod() {
        JavaResourceEntityResolver resolver = new JavaResourceEntityResolver();
        resolver.mapSystemIdentifierToResource("testxml", "/org/objectweb/celtix/tools/common/toolspec/parse"
                                                          + "r/resources/testtool.xml");
        assertTrue(resolver.resolveEntity("testPublicID", "testxml") != null);

        JavaResourceEntityResolver newResolver = (JavaResourceEntityResolver)resolver.clone();

        assertTrue(newResolver.resolveEntity("testPublicID", "testxml") != null);

        resolver.mapSystemIdentifierPrefixToResourcePrefix("a", "b");
        try {
            resolver.mapSystemIdentifierPrefixToResourcePrefix(null, "b");
        } catch (Exception e) {
            assertTrue(true);
            return;
        }
        assertTrue(false);

    }
}
