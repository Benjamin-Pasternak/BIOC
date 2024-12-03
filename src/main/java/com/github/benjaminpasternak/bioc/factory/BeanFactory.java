package com.github.benjaminpasternak.bioc.factory;

import com.github.benjaminpasternak.bioc.annotations.Inject;
import com.github.benjaminpasternak.bioc.exceptions.BeanInstantiationException;
import com.github.benjaminpasternak.bioc.exceptions.ConstructorSelectionException;
import com.github.benjaminpasternak.bioc.exceptions.CyclicDependencyException;
import com.github.benjaminpasternak.bioc.registry.BeanRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * The BeanFactory is responsible for creating instances of beans within the IoC container.
 * It handles:
 * - Constructor selection: Finds the appropriate constructor to use for injection, prioritizing those annotated with @Inject.
 * - Dependency resolution: Resolves and injects dependencies for constructor parameters by interacting with the BeanRegistry.
 * - Bean instantiation: Uses reflection to instantiate objects and return them to the caller.
 *
 * This class separates the logic of bean creation from the storage and retrieval responsibilities of the BeanRegistry.
 *
 * Future Considerations:
 * - Support for optional dependencies.
 * - Integration with field and setter injection.
 * - Proxy-based bean creation for AOP or advanced features.
 */

public class BeanFactory {

    private final BeanRegistry beanRegistry;

    public BeanFactory(BeanRegistry beanRegistry) {
        this.beanRegistry = beanRegistry;
    }

    public <T> T create(Class<T> type) {
        // create from the constructor
        return createWithConstructor(type, new HashSet<>());
    }

    /**
     * Instantiates an instance of a type and its dependencies recursively,
     * adding the created beans to the bean registry as needed.
     * This method is intended for constructor-based dependency injection
     * and is called by the {@code create(Class<T> type)} method.
     *
     * @param type The type of object to instantiate. For example, {@code Shape}.
     * @param <T> The generic type of the object being instantiated.
     * @return The fully instantiated object of the specified type.
     * @throws CyclicDependencyException if a circular dependency is detected during resolution.
     * @throws BeanInstantiationException if the object or one of its dependencies cannot be instantiated.
     */
    private <T> T createWithConstructor(Class<T> type) {
        return createWithConstructor(type, new HashSet<>());
    }

    /**
     * Instantiates an instance of a type and its dependencies recursively,
     * adding the created beans to the bean registry as needed.
     * This method is intended for constructor-based dependency injection
     * and is called by the {@code create(Class<T> type)} method.
     *
     * @param type The type of object to instantiate. For example, {@code Shape}.
     * @param visited A set of visited classes, used to track dependencies currently being resolved
     *                and detect circular dependencies during the resolution process.
     * @param <T> The generic type of the object being instantiated.
     * @return The fully instantiated object of the specified type.
     * @throws CyclicDependencyException if a circular dependency is detected during resolution.
     * @throws BeanInstantiationException if the object or one of its dependencies cannot be instantiated.
     */
    private <T> T createWithConstructor(Class<T> type, Set<Class<?>> visited) {
        if (visited.contains(type)) {
            throw new CyclicDependencyException("Cyclic dependency detected for type: " + type.getName());
        }

        try {
            visited.add(type);

            Constructor<?> constructor = getConstructor(type);
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];

            // instantiate each parameter type
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = createWithConstructor(parameterTypes[i], visited);
            }

            // create and register the instance
            T instance = (T) constructor.newInstance(parameters);
            beanRegistry.register(type, instance);

            return instance;

        } catch(Exception e) {
            throw new BeanInstantiationException("Unable to insantiate bean of type: " + type.getName(), e);
        } finally {
            /**
             * Have to clean up after since visited travels up the call stack.
             * So for instance:
             * Controller(ServiceA, ServiceB) - ServiceA and ServiceB being dependencies of the controller
             * ServiceA(ServiceC)
             * ServiceB(ServiceC)
             * Both ServiceA and ServiceB have a common dependency.
             * If we were trying to resolve these without cleanup, we will detect a circular dependency on accident.
             * If we're at the step where we resolve ServiceA, We'd capture ServiceC in visited. And then if we move to
             * ServiceB it will find ServiceC again and throw the CyclicDependencyException since its already in the
             * visited set. This is wrong and there is no circular dependency. This cleanup is a stopgap to prevent
             * such a situation.
             */
            visited.remove(type);
        }
    }

    /**
     * Injects dependencies into the fields of the given instance.
     * This method is intended to be used after the object has been created and registered.
     *
     * @param instance The instance whose dependencies need to be injected.
     * @param <T> The type of the instance.
     * @throws BeanInstantiationException If a dependency cannot be resolved or injected.
     */
    private <T> void createFromField(T instance) {
        // get the fields
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            // only inject ones we need to inject
            if (field.isAnnotationPresent(Inject.class)) {
                // conformity check
                if (Modifier.isStatic(field.getModifiers())) {
                    throw new UnsupportedOperationException("Cannot Inject into static fields: " + field.getName());
                }

                if (Modifier.isFinal(field.getModifiers())) {
                    throw new UnsupportedOperationException("Cannot Inject into final fields: " + field.getName());
                }
                try {
                    Class<?> fieldType = field.getType();
                    // if we've already created this dependency we can simply reuse the one in bean registry
                    Object dependency = beanRegistry.containsBean(fieldType)
                            ? beanRegistry.resolve(fieldType)
                            : create(fieldType);

                    // ensure we can access the field
                    field.setAccessible(true);
                    // set the field
                    field.set(instance, dependency);
                } catch (Exception e) {
                    throw new BeanInstantiationException("Failed to inject field: " + field.getName() +
                            "in class: " + instance.getClass().getName(), e);
                }
            }
        }
    }




    /**
     * This method is responsible for picking the right constructor for dependency injection.
     * The goal is to find a constructor that can be used to instantiate the given type:
     *
     * Branches:
     * 1. If there’s one constructor annotated with {@code @Inject}, use it. This is the ideal case.
     * 2. If more than one constructor has {@code @Inject}, throw an exception—ambiguity is bad.
     * 3. If no {@code @Inject} is present:
     *    - If there’s only one constructor, we’ll use that.
     *    - If there are multiple constructors and none are annotated, throw an exception—too ambiguous.
     *
     * @param type The class type for which a constructor is needed.
     * @return The selected constructor.
     * @throws ConstructorSelectionException If no suitable constructor can be identified or if there’s ambiguity.
     */
    private Constructor<?> getConstructor(Class<?> type) throws ConstructorSelectionException {
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

}
