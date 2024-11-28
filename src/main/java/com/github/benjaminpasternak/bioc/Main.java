package com.github.benjaminpasternak.bioc;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Logger;

public class Main {
    public static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        setupLogger();
        log.info("Application Started: Hello World");
    }

    private static void setupLogger() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
}
