/*
 * Copyright (C) 2020-present the original author or authors.
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
package org.javaloong.kongmink.pf4j.spring.util;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Get {@link ApplicationContext} in static way. Since {@link ApplicationContext}
 * is bound to static class in this way, Plugin shouldn't use it directly, but
 * need to inherit it as a new private class and register it in its own configuration.
 *
 * To register main {@link ApplicationContext}:
 * 
 * <blockquote><pre>
 * &#064;Bean
 * public ApplicationContextAware multiApplicationContextProviderRegister() {
 *     return ApplicationContextProvider::registerApplicationContext;
 * }
 * </pre></blockquote>
 * 
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class ApplicationContextProvider {

    private static Map<ClassLoader, ApplicationContext> ctxCache = Collections.synchronizedMap(new HashMap<>());

    public static void registerApplicationContext(ApplicationContext ctx) {
        ctxCache.put(ctx.getClassLoader(), ctx);
    }

    public static void unregisterApplicationContext(ApplicationContext ctx) {
        ctxCache.remove(ctx.getClassLoader());
    }

    public static ApplicationContext getApplicationContext(Object probe) {
        return getApplicationContext(probe.getClass().getClassLoader());
    }

    public static ApplicationContext getApplicationContext(Class<?> probeClazz) {
        return getApplicationContext(probeClazz.getClassLoader());
    }

    public static ApplicationContext getApplicationContext(ClassLoader classLoader) {
        return ctxCache.get(classLoader);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getBean(clazz.getClassLoader(), clazz);
    }

    public static <T> T getBean(Class<?> probeClazz, Class<T> clazz) {
        return getBean(probeClazz.getClassLoader(), clazz);
    }

    public static <T> T getBean(ClassLoader classLoader, Class<T> clazz) {
        ApplicationContext ctx = getApplicationContext(classLoader);
        if (ctx == null) return null;
        try {
            return ctx.getBean(clazz);
        } catch (NoSuchBeanDefinitionException ex) {
            return null;
        }
    }

    public static <T> T getBean(Class<?> probeClazz, String beanName) {
        return getBean(probeClazz.getClassLoader(), beanName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(ClassLoader classLoader, String beanName) {
        ApplicationContext ctx = getApplicationContext(classLoader);
        if (ctx == null) return null;
        try {
            return (T) ctx.getBean(beanName);
        } catch (NoSuchBeanDefinitionException ex) {
            return null;
        }
    }

    public static String getMessage(Class<?> probeClazz, String msgKey, Object...params) {
        return getMessage(probeClazz.getClassLoader(), msgKey, params);
    }

    public static String getMessage(ClassLoader classLoader, String msgKey, Object...params) {
        assert ctxCache.containsKey(classLoader);
        try {
            return Optional.ofNullable(getBean(classLoader, MessageSource.class)).map(bean -> bean.getMessage(
                    msgKey, params, LocaleContextHolder.getLocale())).orElse(msgKey);
        } catch (NoSuchMessageException ignored) {
            return msgKey;
        }
    }

}