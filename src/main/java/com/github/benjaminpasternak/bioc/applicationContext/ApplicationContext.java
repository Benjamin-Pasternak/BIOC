package com.github.benjaminpasternak.bioc.applicationContext;

public interface ApplicationContext {
    <T> T getBean(Class<T> type);
    <T> T getBean(Class<T> type, String qualifier);
    boolean containsBean(Class<?> type);
    boolean containsBean(Class<?> type, String qualifier);
    void refresh(); // rebuld app context
}
