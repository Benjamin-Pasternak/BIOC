package com.github.benjaminpasternak.bioc.factory;

import com.github.benjaminpasternak.bioc.annotations.Inject;
import com.github.benjaminpasternak.bioc.exceptions.ConstructorSelectionException;

import java.lang.reflect.Constructor;
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
//    <T> T create() {}

    /**
     * This method is responsible for picking the right constructor for dependency injection.
     * The goal is to find a constructor that can be used to instantiate the given type:
     *
     * Branches:
     * 1. If there’s one constructor annotated with `@Inject`, use it. This is the ideal case.
     * 2. If more than one constructor has `@Inject`, throw an exception—ambiguity is bad.
     * 3. If no `@Inject` is present:
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

        // Case: One constructor and annotated with @Inject
        if (annotatedConstructors.size() == 1) {
            return annotatedConstructors.get(0);
        }

        // Case: More than one constructor has @Inject -> Ambiguity == bad
        if (annotatedConstructors.size() > 1) {
            throw new ConstructorSelectionException("Multiple constructors annotated with @Inject in " + type.getName());
        }

        // Case: No constructor with @Inject
        if (constructors.length == 1) {
            return constructors[0]; // Use the only available constructor
        } else {
            throw new ConstructorSelectionException("No constructor annotated with @Inject, and multiple constructors found in " + type.getName());
        }
    }


}
