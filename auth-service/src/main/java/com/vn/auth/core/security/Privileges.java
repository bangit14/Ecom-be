package com.vn.auth.core.security;

import com.vn.auth.core.enums.RoleAccountEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Privileges {
    RoleAccountEnum[] value() default {};
}
