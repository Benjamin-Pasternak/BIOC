package com.github.benjaminpasternak.bioc.applicationContext;

import com.github.benjaminpasternak.bioc.beanDefinition.BeanDefinition;
import com.github.benjaminpasternak.bioc.beanDefinition.BeanDefinitionScanner;
import com.github.benjaminpasternak.bioc.registry.BeanRegistry;
import com.github.benjaminpasternak.bioc.registry.DefaultBeanRegistry;

import java.util.List;

public class DefaultApplicationContext implements ApplicationContext {
    private final BeanRegistry beanRegistry;
    private final BeanFactoryRefactor beanFactory;
    private final List<BeanDefinition> beanDefinitions;
    private volatile boolean refreshed = false; // visibility across threads


    public DefaultApplicationContext(String basePackage) {
        this.beanRegistry = new DefaultBeanRegistry();
        this.beanFactory = new BeanFactoryRefactor(beanRegistry);
        BeanDefinitionScanner scanner = new BeanDefinitionScanner(basePackage);
        this.beanDefinitions = scanner.scan();
    }

    @Override
    public <T> T getBean(Class<T> type) {
        return beanRegistry.resolve(type);
    }

    @Override
    public <T> T getBean(Class<T> type, String qualifier) {
        return beanRegistry.resolve(type, qualifier);
    }

    @Override
    public boolean containsBean(Class<?> type) {
        return beanRegistry.containsBean(type);
    }

    @Override
    public boolean containsBean(Class<?> type, String qualifier) {
        return beanRegistry.containsBean(type, qualifier);
    }

    // one thread can execute this method at a time with synchronized
    @Override
    public synchronized void refresh() {
        if (!refreshed) {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                if (beanDefinition.isSingleton()) {
                    beanFactory.createBean(beanDefinition);
                }
            }
            refreshed = true;
        }
    }
}
