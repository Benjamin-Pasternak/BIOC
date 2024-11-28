package com.github.benjaminpasternak.bioc.registry;

import com.github.benjaminpasternak.bioc.testUtil.Circle;
import com.github.benjaminpasternak.bioc.testUtil.Rectangle;
import com.github.benjaminpasternak.bioc.testUtil.Shape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultBeanRegistryTest {

    private DefaultBeanRegistry beanRegistry;

    @BeforeEach
    void setUp() {
        beanRegistry = new DefaultBeanRegistry();
    }

    // Tests for register(Class<?> type, String qualifier, Object instance)
    @Test
    void testRegisterQualifiedBean() {
        Shape circle = new Circle();
        beanRegistry.register(Shape.class, "circle", circle);

        Shape resolvedCircle = beanRegistry.resolve(Shape.class, "circle");
        assertNotNull(resolvedCircle, "Circle bean should be resolved.");
        assertEquals(circle, resolvedCircle, "Resolved Circle should match the registered instance.");
    }

    @Test
    void testRegisterQualifiedBeanWithNullValuesThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> beanRegistry.register(null, "circle", new Circle()));
        assertThrows(IllegalArgumentException.class, () -> beanRegistry.register(Shape.class, null, new Circle()));
        assertThrows(IllegalArgumentException.class, () -> beanRegistry.register(Shape.class, "circle", null));
    }

    // Tests for register(Class<?> type, Object instance)
    @Test
    void testRegisterDefaultBean() {
        Shape circle = new Circle();
        beanRegistry.register(Shape.class, circle);

        Shape resolvedDefault = beanRegistry.resolve(Shape.class);
        assertNotNull(resolvedDefault, "Default bean should be resolved.");
        assertEquals(circle, resolvedDefault, "Resolved default bean should match the registered instance.");
    }

    @Test
    void testRegisterDefaultBeanOverwritesPrevious() {
        Shape circle = new Circle();
        Shape rectangle = new Rectangle();

        beanRegistry.register(Shape.class, circle);
        beanRegistry.register(Shape.class, rectangle);

        Shape resolvedDefault = beanRegistry.resolve(Shape.class);
        assertEquals(rectangle, resolvedDefault, "Default bean should match the most recently registered instance.");
    }

    // Tests for resolve(Class<T> type, String qualifier)
    @Test
    void testResolveQualifiedBean() {
        Shape rectangle = new Rectangle();
        beanRegistry.register(Shape.class, "rectangle", rectangle);

        Shape resolvedRectangle = beanRegistry.resolve(Shape.class, "rectangle");
        assertNotNull(resolvedRectangle, "Rectangle bean should be resolved.");
        assertEquals(rectangle, resolvedRectangle, "Resolved Rectangle should match the registered instance.");
    }

    @Test
    void testResolveQualifiedBeanWithMissingTypeThrowsException() {
        assertThrows(RuntimeException.class, () -> beanRegistry.resolve(Shape.class, "circle"),
                "Resolving an unregistered qualified bean should throw an exception.");
    }

    @Test
    void testResolveQualifiedBeanWithMissingQualifierThrowsException() {
        Shape circle = new Circle();
        beanRegistry.register(Shape.class, "circle", circle);

        assertThrows(RuntimeException.class, () -> beanRegistry.resolve(Shape.class, "rectangle"),
                "Resolving a missing qualifier should throw an exception.");
    }

    // Tests for resolve(Class<T> type)
    @Test
    void testResolveDefaultBeanWithMissingTypeThrowsException() {
        assertThrows(RuntimeException.class, () -> beanRegistry.resolve(Shape.class),
                "Resolving a missing default bean should throw an exception.");
    }

    // Tests for containsBean(Class<?> type, String qualifier)
    @Test
    void testContainsBeanWithQualifier() {
        Shape circle = new Circle();
        beanRegistry.register(Shape.class, "circle", circle);

        assertTrue(beanRegistry.containsBean(Shape.class, "circle"), "Circle bean should be registered.");
        assertFalse(beanRegistry.containsBean(Shape.class, "rectangle"), "Rectangle bean should not be registered.");
    }

    @Test
    void testContainsBeanWithNullValuesThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> beanRegistry.containsBean(null, "circle"));
        assertThrows(IllegalArgumentException.class, () -> beanRegistry.containsBean(Shape.class, null));
    }

    // Tests for containsBean(Class<?> type)
    @Test
    void testContainsDefaultBean() {
        Shape circle = new Circle();
        beanRegistry.register(Shape.class, circle);

        assertTrue(beanRegistry.containsBean(Shape.class), "Default bean should be registered.");
    }

    // Tests for getQualifiers(Class<?> type)
    @Test
    void testGetQualifiers() {
        Shape circle = new Circle();
        Shape rectangle = new Rectangle();

        beanRegistry.register(Shape.class, circle);
        beanRegistry.register(Shape.class, "rectangle", rectangle);

        Set<String> qualifiers = beanRegistry.getQualifiers(Shape.class);

        assertEquals(2, qualifiers.size(), "There should be two qualifiers.");
        assertTrue(qualifiers.contains("__default__"), "Default qualifier should be present.");
        assertTrue(qualifiers.contains("rectangle"), "Rectangle qualifier should be present.");
    }

    @Test
    void testGetQualifiersWithUnregisteredTypeReturnsEmptySet() {
        Set<String> qualifiers = beanRegistry.getQualifiers(Shape.class);
        assertTrue(qualifiers.isEmpty(), "Unregistered type should return an empty set of qualifiers.");
    }

    // Tests for deregister(Class<?> type, String qualifier)
    @Test
    void testDeregisterQualifiedBean() {
        Shape circle = new Circle();
        beanRegistry.register(Shape.class, "circle", circle);

        beanRegistry.deregister(Shape.class, "circle");

        assertFalse(beanRegistry.containsBean(Shape.class, "circle"), "Circle bean should be deregistered.");
    }

    @Test
    void testDeregisterQualifiedBeanWithMissingQualifier() {
        Shape circle = new Circle();
        beanRegistry.register(Shape.class, "circle", circle);

        beanRegistry.deregister(Shape.class, "rectangle");

        assertTrue(beanRegistry.containsBean(Shape.class, "circle"), "Circle bean should still be registered.");
    }

    // Tests for deregister(Class<?> type)
    @Test
    void testDeregisterDefaultBean() {
        Shape circle = new Circle();
        beanRegistry.register(Shape.class, circle);

        beanRegistry.deregister(Shape.class);

        assertFalse(beanRegistry.containsBean(Shape.class), "Default bean should be deregistered.");
    }

    @Test
    void testRegisterWithNullTypeThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> beanRegistry.register(null, "circle", new Circle()),
                "Registering with null type should throw IllegalArgumentException.");
    }

    @Test
    void testRegisterWithNullQualifierThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> beanRegistry.register(Shape.class, null, new Circle()),
                "Registering with null qualifier should throw IllegalArgumentException.");
    }

    @Test
    void testRegisterWithNullInstanceThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> beanRegistry.register(Shape.class, "circle", null),
                "Registering with null instance should throw IllegalArgumentException.");
    }

    @Test
    void testResolveWithNullTypeThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> beanRegistry.resolve(null, "circle"),
                "Resolving with null type should throw IllegalArgumentException.");
    }

    @Test
    void testResolveWithNullQualifierThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> beanRegistry.resolve(Shape.class, null),
                "Resolving with null qualifier should throw IllegalArgumentException.");
    }

    @Test
    void testResolveWithMissingTypeThrowsException() {
        assertThrows(RuntimeException.class,
                () -> beanRegistry.resolve(Shape.class, "circle"),
                "Resolving a missing type should throw RuntimeException.");
    }

    @Test
    void testResolveWithMissingQualifierThrowsException() {
        Shape circle = new Circle();
        beanRegistry.register(Shape.class, "circle", circle);

        assertThrows(RuntimeException.class,
                () -> beanRegistry.resolve(Shape.class, "rectangle"),
                "Resolving a missing qualifier should throw RuntimeException.");
    }

    @Test
    void testGetQualifiersWithNullTypeThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> beanRegistry.getQualifiers(null),
                "Getting qualifiers with null type should throw IllegalArgumentException.");
    }

    @Test
    void testGetQualifiersForUnregisteredTypeReturnsEmptySet() {
        Set<String> qualifiers = beanRegistry.getQualifiers(Shape.class);

        assertNotNull(qualifiers, "Qualifiers set should not be null.");
        assertTrue(qualifiers.isEmpty(), "Unregistered type should return an empty set.");
    }

    @Test
    void testDeregisterWithNullTypeThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> beanRegistry.deregister(null, "circle"),
                "Deregistering with null type should throw IllegalArgumentException.");
    }

    @Test
    void testDeregisterWithNullQualifierThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> beanRegistry.deregister(Shape.class, null),
                "Deregistering with null qualifier should throw IllegalArgumentException.");
    }




}
