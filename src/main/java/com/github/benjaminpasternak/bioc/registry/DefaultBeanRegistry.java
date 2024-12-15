package com.github.benjaminpasternak.bioc.registry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultBeanRegistry implements BeanRegistry {

    private final Map<Class<?>, Map<String, Object>> beanRegistry = new ConcurrentHashMap<>();

    @Override
    public void register(Class<?> type, String qualifier, Object instance) {
        if (type == null || qualifier == null || instance == null) {
            throw new IllegalArgumentException("Type, qualifier, and instance must be non-null.");
        }
        beanRegistry.computeIfAbsent(type, k -> new ConcurrentHashMap<>())
                .put(qualifier, instance);
    }

    @Override
    public void register(Class<?> type, Object instance) {
        if (type == null || instance == null) {
            throw new IllegalArgumentException("Type and instance must be non-null.");
        }
        register(type, "__default__", instance);
    }

    @Override
    public <T> T resolve(Class<T> type, String qualifier) {
        if (type == null || qualifier == null) {
            throw new IllegalArgumentException("Type and qualifier must be non-null");
        }
        Map<String, Object> beans = beanRegistry.get(type);
        if (beans == null) {
            throw new RuntimeException("No beans found of type: " + type.getName());
        }
        Object bean = beans.get(qualifier);
        if (bean == null) {
            throw new RuntimeException("No bean found for qualifier: " + qualifier);
        }
        return type.cast(bean);
    }

    @Override
    public <T> T resolve(Class<T> type) {
        return resolve(type, "__default__");
    }

    @Override
    public boolean containsBean(Class<?> type, String qualifier) {
        if (type == null || qualifier == null) {
            throw new IllegalArgumentException("Type and qualifier must be non-null.");
        }
        Map<String, Object> beans = beanRegistry.get(type);
        return beans != null && beans.containsKey(qualifier);
    }

    @Override
    public boolean containsBean(Class<?> type) {
        return containsBean(type, "__default__");
    }

    @Override
    public Set<String> getQualifiers(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type must be non-null.");
        }
        Map<String, Object> beans = beanRegistry.get(type);
        // returns immutable copy of the key set
        return beans != null ? Set.copyOf(beans.keySet()) : Set.of();
    }

    @Override
    public void deregister(Class<?> type, String qualifier) {
        if (type == null || qualifier == null) {
            throw new IllegalArgumentException("Type and qualifier must be non-null");
        }
        Map<String, Object> beans = beanRegistry.get(type);
        if (beans != null) {
            // remove the specific quantifier - instance mapping
            beans.remove(qualifier);
            if (beans.isEmpty()) {
                // if no more quantifiers exist remove the type from the registry
                beanRegistry.remove(type);
            }
        }
    }

    @Override
    public void deregister(Class<?> type) {
        deregister(type, "__default__");
    }
}
