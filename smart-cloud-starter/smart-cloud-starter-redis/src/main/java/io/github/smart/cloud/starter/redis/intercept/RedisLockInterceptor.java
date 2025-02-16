/*
 * Copyright © 2019 collin (1634753825@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.smart.cloud.starter.redis.intercept;

import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import io.github.smart.cloud.constants.SymbolConstant;
import io.github.smart.cloud.exception.AcquiredLockFailException;
import io.github.smart.cloud.starter.redis.annotation.RedisLock;
import io.github.smart.cloud.starter.redis.constants.RedisLockConstants;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * {@link RedisLock}拦截器
 *
 * @author collin
 * @date 2022-02-02
 */
@RequiredArgsConstructor
public class RedisLockInterceptor implements MethodInterceptor {

    private static final LocalVariableTableParameterNameDiscoverer DISCOVERER = new LocalVariableTableParameterNameDiscoverer();
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private final RedissonClient redissonClient;

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        RedisLock redisLock = method.getAnnotation(RedisLock.class);
        EvaluationContext evaluationContext = getEvaluationContext(invocation.getMethod(), invocation.getArguments());
        String lockName = getLockName(redisLock, method.getName(), evaluationContext);
        RLock lock = redissonClient.getLock(lockName);
        boolean lockState = false;
        try {
            if (redisLock.leaseTime() == RedisLockConstants.DEFAULT_LEASE_TIME) {
                lockState = lock.tryLock(redisLock.waitTime(), redisLock.unit());
            } else {
                lockState = lock.tryLock(redisLock.waitTime(), redisLock.leaseTime(), redisLock.unit());
            }
            if (!lockState) {
                throw new AcquiredLockFailException(redisLock.acquiredFailCode());
            }

            return invocation.proceed();
        } finally {
            if (lockState) {
                lock.unlock();
            }
        }
    }

    /**
     * 获取EvaluationContext对象
     *
     * @param method
     * @param arguments
     * @return
     */
    private final EvaluationContext getEvaluationContext(Method method, Object[] arguments) {
        String[] parameterNames = DISCOVERER.getParameterNames(method);
        EvaluationContext evaluationContext = new StandardEvaluationContext(method);
        for (int i = 0; i < parameterNames.length; i++) {
            evaluationContext.setVariable(parameterNames[i], arguments[i]);
        }
        return evaluationContext;
    }

    /**
     * 获取锁名称
     *
     * @param redisLock
     * @param methodName
     * @param evaluationContext
     * @return
     */
    private final String getLockName(RedisLock redisLock, String methodName, EvaluationContext evaluationContext) {
        StringBuilder lockName = new StringBuilder(32);
        lockName.append(RedisLockConstants.DEFAULT_KEY_PREFIX);
        if (StringUtils.isNotBlank(redisLock.keyPrefix())) {
            lockName.append(redisLock.keyPrefix());
            if (redisLock.keyPrefix().endsWith(SymbolConstant.COLON)) {
                lockName.delete(lockName.length() - 1, lockName.length());
            }
        } else {
            lockName.append(methodName);
        }

        String[] expressions = redisLock.expressions();
        for (String expression : expressions) {
            lockName.append(SymbolConstant.COLON);
            lockName.append(PARSER.parseExpression(expression).getValue(evaluationContext));
        }

        return lockName.toString();
    }

}