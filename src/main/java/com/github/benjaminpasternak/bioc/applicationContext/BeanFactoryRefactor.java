package com.github.benjaminpasternak.bioc.applicationContext;

import com.github.benjaminpasternak.bioc.annotations.Inject;
import com.github.benjaminpasternak.bioc.annotations.Named;
import com.github.benjaminpasternak.bioc.beanDefinition.BeanDefinition;
import com.github.benjaminpasternak.bioc.exceptions.BeanInstantiationException;
import com.github.benjaminpasternak.bioc.exceptions.ConstructorSelectionException;
import com.github.benjaminpasternak.bioc.exceptions.CyclicDependencyException;
import com.github.benjaminpasternak.bioc.registry.BeanRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeanFactoryRefactor {
    private final BeanRegistry registry;
    private final ThreadLocal<Set<Class<?>>> constructingBeans = ThreadLocal.withInitial(HashSet::new);

    public BeanFactoryRefactor(BeanRegistry registry) {
        this.registry = registry;
    }

    public <T> T createBean(BeanDefinition beanDefinition) {
        Class<T> type = beanDefinition.getBeanType(); // Updated to use generics

        // If the bean already exists in the registry, resolve it
        if (registry.containsBean(type, beanDefinition.getQualifier()) &&
                beanDefinition.isSingleton()) {
            return registry.resolve(type, beanDefinition.getQualifier());
        }

        // Circular dependency detection
        Set<Class<?>> inProgress = constructingBeans.get();
        if (inProgress.contains(type)) {
            throw new CyclicDependencyException("Cyclic dependency detected for bean: " + type.getName());
        }

        try {
            inProgress.add(type);
            T instance = instantiateBean(type, beanDefinition.getQualifier());
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
        } finally {
            inProgress.remove(type);
        }
    }

    private <T> T instantiateBean(Class<T> type, String qualifier) {
        try {
            Constructor<T> constructor = selectConstructor(type); // Generic constructor
            Object[] dependencies = resolveDependencies(constructor);
            return constructor.newInstance(dependencies);
        } catch (Exception e) {
            throw new BeanInstantiationException("Unable to instantiate bean of type: " + type.getName(), e);
        }
    }

    @SuppressWarnings("unchecked") // this is totally safe since the  T in selectConstructor(Class<T> type) is derived directly from the input Class<T> type
    private <T> Constructor<T> selectConstructor(Class<T> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        // Filter constructors annotated with @Inject
        List<Constructor<T>> injectConstructors = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                injectConstructors.add((Constructor<T>) constructor);
            }
        }

        // Case 1: Single @Inject constructor
        if (injectConstructors.size() == 1) {
            Constructor<T> selected = injectConstructors.get(0);
            selected.setAccessible(true);
            return selected;
        }

        // Case 2: Multiple @Inject constructors
        if (injectConstructors.size() > 1) {
            throw new ConstructorSelectionException(
                    "Multiple constructors annotated with @Inject found in " + type.getName() +
                            ". Ensure only one constructor is annotated with @Inject."
            );
        }

        // Case 3: No @Inject constructor, look for a no-arg constructor
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                return (Constructor<T>) constructor;
            }
        }

        // Case 4: No suitable constructor found
        throw new ConstructorSelectionException(
                "No suitable constructor found for " + type.getName() +
                        ". Ensure the class has a no-arg constructor or one constructor annotated with @Inject."
        );
    }


    // this is O(n^2) though over a small set, not really a trivial way to reduce this so :shrug:
    private <T> Object[] resolveDependencies(Constructor<T> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

        Object[] objects = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            Annotation[] typeParameterAnnotations = parameterAnnotations[i];

            String qualifier = null;
            for (Annotation annotation : typeParameterAnnotations) {
                if (annotation instanceof Named) {
                    Named named = (Named) annotation;
                    qualifier = named.value();
                    break; // Look for @Named annotation
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


    public <T> void injectFields(T instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // conformity check
                validateModifiers(field);
                try {
                    Class<?> fieldType = field.getType();
                    String qualifier = extractQualifier(field.getDeclaredAnnotations());

                    Object dependency = qualifier != null
                            ? registry.resolve(fieldType, qualifier)
                            : registry.resolve(fieldType);
                    field.setAccessible(true);
                    field.set(instance, dependency);
                } catch (Exception e) {
                    throw new BeanInstantiationException("Failed to inject field: " + field.getName() +
                            "in class: " + instance.getClass().getName(), e);
                }
            }
        }
    }

    private void validateModifiers(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new UnsupportedOperationException("Cannot Inject into static fields: " + field.getName());
        }

        if (Modifier.isFinal(field.getModifiers())) {
            throw new UnsupportedOperationException("Cannot Inject into final fields: " + field.getName());
        }
    }

    private String extractQualifier(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Named) {
                return ((Named) annotation).value();
            }
        }
        return null;
    }



    public <T> void injectSetters(T instance) {
        Class<?> clazz = instance.getClass();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (isValidSetter(method)) {
                try {
                    Class<?> parameterType = method.getParameterTypes()[0];
                    String qualifier = extractQualifier(method.getParameterAnnotations()[0]);
                    Object parameterInstance = (qualifier != null)
                            ? registry.resolve(parameterType, qualifier)
                            : registry.resolve(parameterType);
                    method.setAccessible(true);
                    method.invoke(instance, parameterInstance);
                } catch (Exception e) {
                    throw new BeanInstantiationException("Failed to inject bean into setter: " + method.getName(), e);
                }
            }
        }
    }


    private boolean isValidSetter(Method method) {
        if (!method.isAnnotationPresent(Inject.class)) {
            return false;
        }

        if (!method.getName().startsWith("set") || method.getParameterCount() != 1) {
            return false;
        }

        if (Modifier.isStatic(method.getModifiers())) {
            return false;
        }

        return true;
    }
}
