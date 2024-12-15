package com.github.benjaminpasternak.bioc.registry;


import com.github.benjaminpasternak.bioc.test.Rectangle;
import com.github.benjaminpasternak.bioc.test.Shape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultBeanRegistryTest {

    private BeanRegistry beanRegistry;

    @BeforeEach
    void init() {
        beanRegistry = new DefaultBeanRegistry();
    }

    @Test
    void registerWithQualifierTest() {
        Class<Shape> shapeClass = Shape.class;
        String qualifier = "foobar";
        Shape instance = new Rectangle();

        beanRegistry.register(shapeClass, qualifier, instance);

        assertTrue(beanRegistry.containsBean(shapeClass, qualifier));
        assertEquals(instance, beanRegistry.resolve(shapeClass, qualifier));
    }

    @Test
    void registerWithQualifier_nullType() {
        Class<Shape> type = null;
        String qualifier = "foobar";
        Shape instance = new Rectangle();
        assertThrows(IllegalArgumentException.class, () -> beanRegistry.register(type, qualifier, instance));
    }

    @Test
    void registerWithQualifier_nullQualifier() {
        Class<Shape> type = Shape.class;
        String qualifier = null;
        Shape instance = new Rectangle();
        assertThrows(IllegalArgumentException.class, () -> beanRegistry.register(type, qualifier, instance));
    }

    @Test
    void registerWithQualifier_nullInstance() {
        Class<Shape> type = Shape.class;
        String qualifier = "foobar";
        Shape instance = null;
        assertThrows(IllegalArgumentException.class, () -> beanRegistry.register(type, qualifier, instance));
    }

    @Test
    void registerTest() {
        Class<Shape> shapeClass = Shape.class;
        Shape instance = new Rectangle();

        beanRegistry.register(shapeClass, instance);

        assertTrue(beanRegistry.containsBean(shapeClass));
        assertEquals(instance, beanRegistry.resolve(shapeClass));
    }

    @Test
    void register_nullType() {
        Class<Shape> type = null;
        Shape instance = new Rectangle();
        assertThrows(IllegalArgumentException.class, () -> beanRegistry.register(type, instance));
    }

    @Test
    void register_nullInstance() {
        Class<Shape> type = Shape.class;
        Shape instance = null;
        assertThrows(IllegalArgumentException.class, () -> beanRegistry.register(type, instance));
    }

    @Test
    void resolveWithQualifier() {
        Class<Shape> type = Shape.class;
        String qualifier = "foobar";
        Shape instance = new Rectangle();
        // register the bean
        beanRegistry.register(type, qualifier, instance);

        Shape result = beanRegistry.resolve(type, qualifier);

        assertEquals(instance, result);
    }

    @Test
    void resolveWithQualifier_typeNull() {
        Class<Shape> type = Shape.class;
        String qualifier = "foobar";
        Shape instance = new Rectangle();
        // register the bean
        beanRegistry.register(type, qualifier, instance);

        assertThrows(IllegalArgumentException.class, () ->
                beanRegistry.resolve(null, qualifier));
    }

    @Test
    void resolveWithQualifier_qualifierNull() {
        Class<Shape> type = Shape.class;
        String qualifier = "foobar";
        Shape instance = new Rectangle();
        // register the bean
        beanRegistry.register(type, qualifier, instance);

        assertThrows(IllegalArgumentException.class, () ->
                beanRegistry.resolve(type, null));
    }

    @Test
    void resolveWithQualifier_beansNull() {
        Class<Shape> type = Shape.class;
        String qualifier = "foobar";

        assertThrows(RuntimeException.class, () ->
                beanRegistry.resolve(type, qualifier));
    }

    @Test
    void resolveWithQualifier_beanNull() {
        Class<Shape> type = Shape.class;
        String qualifier = "foobar";
        Shape instance = new Rectangle();
        beanRegistry.register(type, instance);

        assertThrows(RuntimeException.class, () ->
                beanRegistry.resolve(type, qualifier));
    }

    @Test
    void resolve() {
        Class<Shape> type = Shape.class;
        Shape instance = new Rectangle();
        beanRegistry.register(type, instance);

        Shape result = beanRegistry.resolve(type);
        assertEquals(instance, result);
    }

    @Test
    void containsBeanWithQualifier() {
        Class<Shape> type = Shape.class;
        String qualifier = "foobar";
        Shape instance = new Rectangle();

        beanRegistry.register(type, qualifier, instance);
        assertTrue(beanRegistry.containsBean(type, qualifier));
    }

    @Test
    void containsBeanWithQualifier_nullType() {
        assertThrows(IllegalArgumentException.class, () ->
                beanRegistry.containsBean(null, "fooBar"));
    }

    @Test
    void containsBeanWithQualifier_nullQualifier() {
        Class<Shape> type = Shape.class;
        assertThrows(IllegalArgumentException.class, () ->
                beanRegistry.containsBean(type, null));
    }

    @Test
    void containsBeanWithQualifier_nullBeans() {
        Class<Shape> type = Shape.class;
        String qualifier = "foobar";

        assertFalse(beanRegistry.containsBean(type, qualifier));
    }

    @Test
    void containsBean() {
        Class<Shape> type = Shape.class;
        Shape instance = new Rectangle();
        beanRegistry.register(type, instance);

        assertTrue(beanRegistry.containsBean(type));
    }

    @Test
    void getQualifiers() {
        Class<Shape> type = Shape.class;
        String qualifier = "foobar";
        Shape instance = new Rectangle();
        beanRegistry.register(type, qualifier, instance);
        Set<String> expected = Set.of("foobar");

        assertEquals(expected, beanRegistry.getQualifiers(type));
    }

    @Test
    void getQualifiers_nullType() {
        assertThrows(IllegalArgumentException.class, () ->
                beanRegistry.getQualifiers(null));
    }

    @Test
    void getQualifiers_emptySet() {
        assertEquals(0, beanRegistry.getQualifiers(Shape.class).size());
    }

   @Test
   void deregisterWithQualifier() {
       Class<Shape> type = Shape.class;
       String qualifier = "foobar";
       Shape instance = new Rectangle();
       beanRegistry.register(type, qualifier, instance);

       beanRegistry.deregister(type, qualifier);
       assertFalse(beanRegistry.containsBean(type, qualifier));
   }

   @Test
   void deregisterWithQualifier_nullType() {
        assertThrows(IllegalArgumentException.class, () ->
                beanRegistry.deregister(null, "foobar"));
   }

    @Test
    void deregisterWithQualifier_nullQualifier() {
        assertThrows(IllegalArgumentException.class, () ->
                beanRegistry.deregister(Shape.class, null));
    }

    @Test
    void deregister() {
        Class<Shape> type = Shape.class;
        String qualifier = "foobar";
        Shape instance = new Rectangle();
        beanRegistry.register(type, instance);

        beanRegistry.deregister(type);
        assertFalse(beanRegistry.containsBean(type));
    }
}
