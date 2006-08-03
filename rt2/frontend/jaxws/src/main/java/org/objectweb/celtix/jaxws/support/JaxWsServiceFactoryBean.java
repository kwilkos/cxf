package org.objectweb.celtix.jaxws.support;

import org.objectweb.celtix.service.factory.ReflectionServiceFactoryBean;

public class JaxWsServiceFactoryBean extends ReflectionServiceFactoryBean {

    public JaxWsServiceFactoryBean() {
        super();

        getServiceConfigurations().add(new JaxWsServiceConfiguration());
    }

}
