package com.github.benjaminpasternak.bioc.beanDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.benjaminpasternak.bioc.annotations.Named;
import com.github.benjaminpasternak.bioc.annotations.Singleton;
import org.reflections.Reflections;

public class BeanDefinitionScanner {
    private final String basePackage;

    public BeanDefinitionScanner(String basePackage) {
        this.basePackage = basePackage;
    }

    public List<BeanDefinition> scan() {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> singletons = reflections.getTypesAnnotatedWith(Singleton.class);

        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        for (Class<?> c : singletons) {
            String qualifier = null;
            BeanDefinition beanDefinition = new BeanDefinition(c, true, qualifier);
            beanDefinitions.add(beanDefinition);
        }
        return beanDefinitions;
    }
}
