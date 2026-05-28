package com.goosebeoms.tickets.global.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String bucket();

    int limit();

    int windowSeconds() default 60;

    KeyType keyType() default KeyType.IP;

    enum KeyType {
        IP,
        USER
    }
}
