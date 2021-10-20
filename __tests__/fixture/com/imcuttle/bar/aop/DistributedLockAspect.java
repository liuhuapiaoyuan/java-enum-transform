/**
 * @(#)DistributedLockAspect.java, 5月 06, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.aop;

import com.fenbi.common.jedis.lock.DistributeLocker;
import com.fenbi.common.jedis.lock.LockTimeoutException;
import com.imcuttle.bar.constant.LockKeyFieldConstant;
import com.imcuttle.bar.enums.LockPrefixEnum;
import com.imcuttle.bar.exceptions.FailedGetLockInfoException;
import com.imcuttle.bar.util.lock.DistributedLock;
import com.imcuttle.bar.util.lock.DistributedLockKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

/**
 * @author zhangzhikuan
 */
@Component
@Aspect
@Slf4j
public class DistributedLockAspect {

    @PostConstruct
    public void init() {
    }

    @Autowired
    private DistributeLocker distributeLocker;

    private static final long MAX_WAIT_TIME_IN_MSEC = 10000L;

    @Around("execution(* com.imcuttle.bar..*(..)) && @annotation(com.imcuttle.bar.util.lock.DistributedLock)")
    public Object lock(ProceedingJoinPoint point) throws Throwable {
        Object object;
        String requestId = UUID.randomUUID().toString();
        DistributedLock redisLock = getRedissonLockInfo(point);
        if (Objects.isNull(redisLock)) {
            log.error("锁信息获取失败");
            throw new FailedGetLockInfoException("锁信息获取失败");
        }
        String lockKey = getKey(redisLock, point);
        if (StringUtils.isEmpty(lockKey)) {
            log.error("参数无法获得正确的lockKey");
            throw new FailedGetLockInfoException("参数无法获得正确的lockKey");
        }
        try {
            log.info("开始尝试加锁，lockKey = {}", lockKey);
            distributeLocker.lock(lockKey, requestId, MAX_WAIT_TIME_IN_MSEC);
            object = point.proceed();
        } catch (LockTimeoutException e) {
            log.error("尝试获得锁超时，lockKey = {}", lockKey, e);
            throw e;
        } finally {
            distributeLocker.unlock(lockKey, requestId);
            log.info("释放锁，lockKey = {}", lockKey);
        }
        return object;
    }

    private DistributedLock getRedissonLockInfo(ProceedingJoinPoint point) {
        try {
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            Method method = methodSignature.getMethod();
            return method.getAnnotation(DistributedLock.class);
        } catch (Exception e) {
            log.error("获取锁信息失败, ", e);
        }
        return null;
    }

    private String getKey(DistributedLock redisLock, ProceedingJoinPoint point) {
        String prefix = getPrefixLockKey(redisLock);
        String suffix = getSuffixLockKey(point);
        if (StringUtils.isEmpty(prefix) || StringUtils.isEmpty(suffix)) {
            return "";
        }
        return prefix + ":" + suffix;
    }

    private String getPrefixLockKey(DistributedLock redisLock) {
        String key = redisLock.prefix().toString();
        //前缀为空或者在前缀枚举中不存在
        if (StringUtils.isEmpty(key) || !LockPrefixEnum.findByString(key).isPresent()) {
            return "";
        }
        return key;
    }

    private String getSuffixLockKey(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                MethodSignature methodSignature = (MethodSignature) point.getSignature();
                Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();
                DistributedLockKey redisLockKey = null;
                int count = 0;
                int index = 0;
                for (int i = 0; i < parameterAnnotations.length; i++) {
                    DistributedLockKey tempLockKey = getAnnotation(DistributedLockKey.class, parameterAnnotations[i]);
                    if (tempLockKey != null) {
                        count ++;
                        index = i;
                        redisLockKey = tempLockKey;
                    }
                }
                //不存在lockKey注解或添加多个lockKey注解
                if (count != 1) {
                    log.error("lockKey注解设置错误");
                    return "";
                }

                String f = redisLockKey.field();
                Object arg = args[index];
                if(StringUtils.isEmpty(f) || Objects.isNull(arg)){
                    log.error("lockKey注解设置错误");
                    return "";
                }
                if (f.equals(LockKeyFieldConstant.PARAMETER_IDENTITY)) {
                    return arg.toString();
                }
                try {
                    Field field = arg.getClass().getDeclaredField(f);
                    field.setAccessible(true);
                    Object value = field.get(arg);
                    if (value != null) {
                        return value.toString();
                    } else {
                        //入参对应字段的值为空
                        log.error("the field named {} is null", f);
                        return "";
                    }
                } catch (NoSuchFieldException exception) {
                    log.error("the field named {} does not exist", f);
                    //入参对应字段不存在
                    return "";
                }
            }
        } catch (Exception e) {
            log.error("getLockKey error.", e);
        }
        return "";
    }

    private static <T extends Annotation> T getAnnotation(final Class<T> annotationClass, final Annotation[] annotations) {
        if (annotations != null && annotations.length > 0) {
            for (final Annotation annotation : annotations) {
                if (annotationClass.equals(annotation.annotationType())) {
                    return (T) annotation;
                }
            }
        }
        return null;
    }
}
