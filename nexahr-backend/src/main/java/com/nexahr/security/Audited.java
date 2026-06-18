package com.nexahr.security;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {
    String action();
    String entityType() default "";
    String details() default "";
}
