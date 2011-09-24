package net.sf.buildbox.releasator;

import java.util.Map;
import net.sf.buildbox.releasator.ng.ScmAdapterFactory;
import net.sf.buildbox.releasator.ng.ScmAdapterManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * See article on <a href="http://www.mydeveloperconnection.com/html/SpringBP.htm">Spring Best Practices</a>
 */
public class SpringBeans {
    public static final SpringBeans INSTANCE = new SpringBeans();
    private ClassPathXmlApplicationContext applicationContext;
    private ScmAdapterManager scmAdapterManager;

    public SpringBeans() {
        applicationContext = new ClassPathXmlApplicationContext("/releasator.spring.xml");
    }

    private <T> T get(String beanName, Class<T> clazz) {
        return applicationContext.getBean(beanName, clazz);
    }

    public ScmAdapterManager getScmAdapterManager() {
        if (scmAdapterManager == null) {
            scmAdapterManager = get("scmAdapterManager", ScmAdapterManager.class);
            scmAdapterManager.setAdapters(getScmAdapterFactories());
        }
        return scmAdapterManager;
    }

    public Map<String, ScmAdapterFactory> getScmAdapterFactories() {
        final Map<String, ScmAdapterFactory> factoryMap = applicationContext.getBeansOfType(ScmAdapterFactory.class);
        System.out.println("factoryMap = " + factoryMap);
        return factoryMap;
    }
}
