package com.github.benjaminpasternak.bioc.factory;

import com.github.benjaminpasternak.bioc.annotations.Inject;
import com.github.benjaminpasternak.bioc.annotations.Named;
import com.github.benjaminpasternak.bioc.beanDefinition.BeanDefinition;
import com.github.benjaminpasternak.bioc.exceptions.BeanInstantiationException;
import com.github.benjaminpasternak.bioc.exceptions.ConstructorSelectionException;
import com.github.benjaminpasternak.bioc.registry.BeanRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class BeanFactoryRefactor {
    private final BeanRegistry registry;

    public BeanFactoryRefactor(BeanRegistry registry) {
        this.registry = registry;
    }

    public Object createBean(BeanDefinition beanDefinition) {
        Class<?> type = beanDefinition.getBeanType();

        // If the bean already exists with in the registry, resolve it
        if(registry.containsBean(type, beanDefinition.getQualifier()) &&
            beanDefinition.isSingleton()) {
            return registry.resolve(type, beanDefinition.getQualifier());
        }

        Object instance = instantiateBean(type, beanDefinition.getQualifier());
        if (beanDefinition.isSingleton()) {
            if (beanDefinition.getQualifier() != null) {
                registry.register(type, beanDefinition.getQualifier(), instance);
            } else {
                registry.register(type, instance);
            }
        }
        injectFields(instance);
        injectSetters(instance);

        return instance;
    }

    private Object instantiateBean(Class<?> type, String qualifier){
        try {
            Constructor<?> constructor = selectConstructor(type);
            Object[] dependencies = resolveDependencies(constructor);
            return constructor.newInstance(dependencies);
        } catch (Exception e) {
            throw new BeanInstantiationException("Unable to instantiate bean of type: " + type.getName(), e);
        }
    }

    private Constructor<?> selectConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        List<Constructor<?>> annotatedConstructors = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                annotatedConstructors.add(constructor);
            }
        }

        if (annotatedConstructors.size() == 1) {
            Constructor<?> selected = annotatedConstructors.get(0);
            selected.setAccessible(true); // for private constructors
            return selected;
        }

        if (annotatedConstructors.size() > 1) {
            throw new ConstructorSelectionException("Multiple constructors annotated with @Inject in " + type.getName());
        }

        if (constructors.length == 1) {
            Constructor<?> selected = constructors[0];
            selected.setAccessible(true); // for private constructors
            return selected;
        }

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true); // darn private constructors
                return constructor;
            }
        }
        throw new ConstructorSelectionException("No @Inject constructor found, and no-arg constructor is unavailable for " + type.getName());
    }

    // this is O(n^2) though over a small set, not really a trivial way to reduce this so :shrug:
    private Object[] resolveDependencies(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

        Object[] objects = new Object[parameterTypes.length];
        for(int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            Annotation[] typeParameterAnnotations = parameterAnnotations[i];

            String qualifier = null;
            for (Annotation annotation : typeParameterAnnotations) {
                if (annotation instanceof Named) {
                    Named named = (Named) annotation;
                    qualifier = named.value();
                    break; // we're just looking for the @Named annotation
                }
            }

            Object dependencyInstance;
            if (qualifier != null) {
                dependencyInstance = registry.resolve(paramType, qualifier);
            } else {
                dependencyInstance = registry.resolve(paramType);
            }

            objects[i] = dependencyInstance;
        }

        return objects;
    }

    public void injectFields(Object instance) {

    }

    public void injectSetters(Object instance) {

    }
}
