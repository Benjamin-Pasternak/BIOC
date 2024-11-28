package com.github.benjaminpasternak.bioc.registry;

import java.util.Set;

/**
 * The BeanRegistry interface defines the contract for managing beans (objects)
 * within a container. It supports default and named registrations.
 */
public interface BeanRegistry {

    /**
     * Registers a bean in the container with a specific qualifier.
     *
     * @param type      The class type of the bean to register. For example, {@code Shape.class}.
     * @param qualifier The unique name or qualifier for the bean.
     * @param instance  The instance of the bean to register.
     *
     * Example Usage:
     * <pre>{@code
     *   // Register two Shape instances with different qualifiers
     *   register(Shape.class, "circle", new Circle());
     *   register(Shape.class, "rectangle", new Rectangle());
     * }</pre>
     */
    void register(Class<?> type, String qualifier, Object instance);

    /**
     * Registers a default bean in the container for a specific type.
     * If a default bean already exists for the type, it is replaced.
     *
     * @param type     The class type of the bean to register. For example, {@code Shape.class}.
     * @param instance The instance of the bean to register as the default.
     *
     * Example Usage:
     * <pre>{@code
     *   // Register a default Shape
     *   register(Shape.class, new Circle());
     * }</pre>
     */
    void register(Class<?> type, Object instance);

    /**
     * Resolves a bean by its type and qualifier.
     *
     * @param <T>       The expected type of the bean to resolve.
     * @param type      The class type of the bean to retrieve.
     * @param qualifier The unique name or qualifier of the bean to retrieve.
     * @return The resolved bean instance of the specified type and qualifier.
     * @throws RuntimeException if no bean of the specified type and qualifier is found.
     *
     * Example Usage:
     * <pre>{@code
     *   // Retrieve the Rectangle instance
     *   Shape rectangle = resolve(Shape.class, "rectangle");
     * }</pre>
     */
    <T> T resolve(Class<T> type, String qualifier);

    /**
     * Resolves the default bean for the specified type.
     *
     * @param <T>  The expected type of the bean to resolve.
     * @param type The class type of the bean to retrieve.
     * @return The resolved default bean instance of the specified type.
     * @throws RuntimeException if no default bean of the specified type is found.
     *
     * Example Usage:
     * <pre>{@code
     *   // Retrieve the default Shape (e.g., Circle)
     *   Shape defaultShape = resolve(Shape.class);
     * }</pre>
     */
    <T> T resolve(Class<T> type);

    /**
     * Checks if a bean of the specified type and qualifier exists in the container.
     *
     * @param type      The class type to check for.
     * @param qualifier The qualifier of the bean to check for.
     * @return {@code true} if a bean of the specified type and qualifier is registered, {@code false} otherwise.
     *
     * Example Usage:
     * <pre>{@code
     *   // Check if a Circle instance is registered
     *   boolean hasCircle = containsBean(Shape.class, "circle");
     * }</pre>
     */
    boolean containsBean(Class<?> type, String qualifier);

    /**
     * Checks if a default bean of the specified type exists in the container.
     *
     * @param type The class type to check for.
     * @return {@code true} if a default bean of the specified type is registered, {@code false} otherwise.
     *
     * Example Usage:
     * <pre>{@code
     *   // Check if a default Shape is registered
     *   boolean hasDefaultShape = containsBean(Shape.class);
     * }</pre>
     */
    boolean containsBean(Class<?> type);

    /**
     * Retrieves all qualifiers registered for a specific type.
     *
     * @param type The class type to retrieve qualifiers for.
     * @return A set of all qualifiers registered for the specified type.
     *
     * Example Usage:
     * <pre>{@code
     *   // Get all qualifiers for Shape
     *   Set<String> shapeQualifiers = getQualifiers(Shape.class); // Returns ["circle", "rectangle"]
     * }</pre>
     */
    Set<String> getQualifiers(Class<?> type);

    /**
     * Deregisters a bean from the container by its type and qualifier.
     *
     * @param type      The class type of the bean to deregister.
     * @param qualifier The qualifier of the bean to deregister.
     * @throws RuntimeException if no bean of the specified type and qualifier is found.
     *
     * Example Usage:
     * <pre>{@code
     *   // Remove the Circle instance
     *   deregister(Shape.class, "circle");
     * }</pre>
     */
    void deregister(Class<?> type, String qualifier);

    /**
     * Deregisters the default bean from the container by its type.
     *
     * @param type The class type of the bean to deregister.
     *
     * Example Usage:
     * <pre>{@code
     *   // Remove the default Shape
     *   deregister(Shape.class);
     * }</pre>
     */
    void deregister(Class<?> type);
}
