package com.github.benjaminpasternak.bioc.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Named {
    String value() default "";
}
