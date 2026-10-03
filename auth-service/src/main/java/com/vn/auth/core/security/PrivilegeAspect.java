package com.vn.auth.core.security;

import com.vn.auth.core.enums.RoleAccountEnum;
import com.vn.auth.core.exception.AppException;
import com.vn.auth.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Slf4j
public class PrivilegeAspect {

    @Around("@annotation(com.vn.auth.core.security.Privileges) || @within(com.vn.auth.core.security.Privileges)")
    public Object checkPrivilege(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Privileges privileges = method.getAnnotation(Privileges.class);
        if (privileges == null) {
            privileges = joinPoint.getTarget().getClass().getAnnotation(Privileges.class);
        }

        if (privileges != null && privileges.value().length > 0) {
            List<String> userRoles = userDetails.getRoles();

            // Admin always has full access (bypass)
            if (userRoles.contains("ADMIN") || userRoles.contains("ROLE_ADMIN")) {
                return joinPoint.proceed();
            }

            boolean hasRequiredRole = Arrays.stream(privileges.value())
                    .anyMatch(role -> userRoles.contains(role.getValue())
                            || userRoles.contains("ROLE_" + role.getValue()));

            if (!hasRequiredRole) {
                log.warn("Access denied for user {} with roles {}. Required: {}",
                        userDetails.getUsername(), userRoles, Arrays.toString(privileges.value()));
                throw new AppException(ErrorCode.FORBIDDEN);
            }
        }

        return joinPoint.proceed();
    }
}
