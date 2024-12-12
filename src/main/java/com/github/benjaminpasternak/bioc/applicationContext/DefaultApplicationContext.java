package com.github.benjaminpasternak.bioc.applicationContext;

import com.github.benjaminpasternak.bioc.factory.BeanFactory;
import com.github.benjaminpasternak.bioc.registry.BeanRegistry;

public class DefaultApplicationContext implements ApplicationContext {
    private final BeanRegistry beanRegistry;
    private final BeanFactory beanFactory;

    public DefaultApplicationContext(BeanRegistry beanRegistry,
                                     BeanFactory beanFactory) {
        this.beanRegistry = beanRegistry;
        this.beanFactory = beanFactory;
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

    @Override
    public void refresh() {

    }
}
