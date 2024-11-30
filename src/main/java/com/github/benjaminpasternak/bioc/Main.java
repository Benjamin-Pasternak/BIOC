package com.github.benjaminpasternak.bioc;

import com.github.benjaminpasternak.bioc.test.Circle;
import com.github.benjaminpasternak.bioc.test.Rectangle;
import com.github.benjaminpasternak.bioc.test.Shape;
import com.github.benjaminpasternak.bioc.test.ShapeService;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import java.util.logging.Logger;

public class Main {
    public static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        setupLogger();
        log.info("Application Started: Hello World");

        Shape circle = new Circle();
        Rectangle rectangle = new Rectangle();

        ShapeService shapeService = new ShapeService(circle, rectangle);
        Class<?> c = shapeService.getClass();
        Constructor[] s = c.getConstructors();
        Annotation[] s2 = s[0].getDeclaredAnnotations();
        String a = s2[0].annotationType().getName();
        String b = s2[0].annotationType().getName().trim();

        log.info("breakpoint");
    }

    private static void setupLogger() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
}
