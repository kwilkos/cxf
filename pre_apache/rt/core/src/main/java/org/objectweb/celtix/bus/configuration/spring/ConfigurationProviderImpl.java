package org.objectweb.celtix.bus.configuration.spring;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.jaxb.JAXBUtils;
import org.objectweb.celtix.tools.generators.spring.SpringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.UrlResource;


public class ConfigurationProviderImpl implements ConfigurationProvider {

    public static final String CONFIG_FILE_PROPERTY_NAME = "celtix.config.file";


    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationProviderImpl.class);
    private static Map<UrlResource, CeltixXmlBeanFactory> beanFactories;

    private Object bean;
    
    @Resource()
    private Configuration configuration;
    
    @Resource()
    private URL url;
    
    public static void clearBeanFactoriesMap() {
        beanFactories = null;
    }

    @PostConstruct
    public void init() {

        if (null == beanFactories) {
            beanFactories = new HashMap<UrlResource, CeltixXmlBeanFactory>();
        }

        CeltixXmlBeanFactory beanFactory =  null;
        UrlResource urlRes = getBeanDefinitionsResource();
        if (null != urlRes) {
            if (!beanFactories.containsKey(urlRes)) {

                if (null != urlRes) {
                    try {
                        beanFactory = new CeltixXmlBeanFactory(urlRes);
                    } catch (BeansException ex) {
                        // continue without using configuration from the bean definitions
                        LOG.log(Level.WARNING, new Message("BEAN_FACTORY_CREATION_MSG", LOG, urlRes
                                                           .toString()).toString(), ex);
                    }
                    beanFactories.put(urlRes, beanFactory);
                }
            } else {
                beanFactory = beanFactories.get(urlRes);
            }
        }

        if (null != beanFactory) {
            beanFactory.registerCustomEditors(configuration);
            findBean(beanFactory);
        } else {
            LOG.fine("Not using a bean definitions file.");
        }

    }

    public Object getObject(String name) {
        // TODO use BeanWrapper instead
        if (null != bean) {
            return invokeGetter(bean, name);
        }
        return null;
    }

    public boolean setObject(String name, Object value) {
        if (null == bean) {
            initBean();
        }

        if (null != bean) {
            return invokeSetter(bean, value, name);
        }

        return false;
    }

    public boolean save() {
        //TODO, two situations:
        //1. the bean is created by spring XMLBeanFactory. As the spring XMLBeanFactory
        //knows BeanDefinition, so it knows how to persist the bean to config file properly.
        //2. The bean is created by ourself. In this case, we can not use spring XMLBeanFactory
        //to do the persistence. We may need to find config schemas and using jaxb to persist
        //bean by ourself
        
        return false;
    }

    protected Object getBean() {
        return bean;
    }

    protected static Map<UrlResource, CeltixXmlBeanFactory> getBeanFactories() {
        return beanFactories;
    }

    private Object invokeGetter(Object beanObject, String name) {

        String methodName = JAXBUtils.nameToIdentifier(name, JAXBUtils.IdentifierType.GETTER);
        try {
            Method m = beanObject.getClass().getMethod("isSet", new Class[] {String.class});
            Object o = m.invoke(beanObject, new Object[] {name});
            if (!((Boolean)o).booleanValue()) {
                return null;
            }
            m = beanObject.getClass().getMethod(methodName, new Class[] {});
            return m.invoke(beanObject);

        } catch (Exception ex) {
            throw new ConfigurationException(new Message("BEAN_INCOVATION_EXC", LOG), ex);
        }
    }

    private boolean invokeSetter(Object beanObject, Object value, String name) {

        String methodName = JAXBUtils.nameToIdentifier(name, JAXBUtils.IdentifierType.SETTER);
        try {
            Class[]  para = new Class[1];

            if (value.getClass() == Integer.class) {
                para[0] = Integer.TYPE;
            } else if (value.getClass() == Float.class) {
                para[0] = Float.TYPE;
            } else if (value.getClass() == Double.class) {
                para[0] = Double.TYPE;
            } else if (value.getClass() == Boolean.class) {
                para[0] = Boolean.TYPE;
            } else if (value.getClass() == Long.class) {
                para[0] = Long.TYPE;
            } else if (value.getClass() == Short.class) {
                para[0] = Short.TYPE;
            } else if (value.getClass() == Character.class) {
                para[0] = Character.TYPE;
            } else if (value.getClass() == Byte.class) {
                para[0] = Byte.TYPE;
            } else {
                para[0] = value.getClass();
            }

            Method m = beanObject.getClass().getMethod(methodName, para);
            m.invoke(beanObject, value);
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ConfigurationException(new Message("BEAN_INCOVATION_EXC", LOG), ex);
        }
    }

    private void initBean() {
        String beanClassName =
            SpringUtils.getBeanClassName(configuration.getModel().getNamespaceURI());
        Class<?> beanClass = null;
        try {
            beanClass = Class.forName(beanClassName);
        } catch (ClassCastException ex) {
            LOG.log(Level.SEVERE, "Could not load bean class  " + beanClassName, ex);
            return;
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "Could not load bean class  " + beanClassName, ex);
            return;
        }

        try {
            bean = beanClass.newInstance();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not create bean instance  " + beanClassName, e);
            return;
        }
    }

    /**
     * get the id of the ancestor configuration and look for a correspondingly named file
     * with extension .xml in the directory pointed to by system property
     * celtix.config.dir
     * @param id
     * @return
     */

    protected UrlResource getBeanDefinitionsResource() {
        UrlResource urlRes = null;
        try {
            if (null == url) {
                String surl = System.getProperty(CONFIG_FILE_PROPERTY_NAME);
                if (null != surl) {
                    url = new URL(surl);
                }
            }
            if (null != url) {
                urlRes = new UrlResource(url);
            } 
        } catch (MalformedURLException ex) {
            // continue using default configuration
            LOG.log(Level.WARNING, new Message("MALFORMED_URL_MSG", LOG, url).toString(), ex);
        }

        return urlRes;
    }

    private void findBean(CeltixXmlBeanFactory beanFactory) {

        String beanClassName = SpringUtils.getBeanClassName(configuration.getModel().getNamespaceURI());
        Class<?> beanClass = null;
        try {
            beanClass = Class.forName(beanClassName);
        } catch (ClassCastException ex) {
            LOG.log(Level.SEVERE, "Could not load bean class  " + beanClassName, ex);
            return;
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "Could not load bean class  " + beanClassName, ex);
            return;
        }

        String[] candidates = beanFactory.getBeanNamesForType(beanClass);
        if (null == candidates || candidates.length == 0) {
            bean = null;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("No definitions for beans of type " + beanClass.getName());
            }
            return;
        }

        List<BeanName> beanNames = new ArrayList<BeanName>();
        for (String n : candidates) {
            BeanName bn = new BeanName(n);
            bn.normalise();
            beanNames.add(bn);
        }

        BeanName ref = new BeanName(configuration);
        BeanName beanName = ref.findBestMatch(beanNames);

        if (null != beanName) {
            try {
                bean = beanFactory.getBean(beanName.getName(), beanClass);
            } catch (NoSuchBeanDefinitionException ex) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Could not find definition for bean with id " + beanName);
                }
            } catch (BeansException ex) {
                throw new ConfigurationException(new Message("BEAN_CREATION_EXC", LOG, beanName), ex);
            }
        }

        if (null == bean && LOG.isLoggable(Level.INFO)) {
            LOG.info("Could not find matching bean definition for component " + ref.getName());
        }
    }
}
