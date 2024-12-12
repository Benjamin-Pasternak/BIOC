package com.github.benjaminpasternak.bioc.beanDefinition;

public class BeanDefinition {
    private final Class<?> beanType;
    private final boolean singleton;
    private final String qualifier; // nullable

    public BeanDefinition(Class<?> beanType, boolean singleton, String qualifier) {
        this.beanType = beanType;
        this.singleton = singleton;
        this.qualifier = qualifier;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public String getQualifier() {
        return qualifier;
    }
}
