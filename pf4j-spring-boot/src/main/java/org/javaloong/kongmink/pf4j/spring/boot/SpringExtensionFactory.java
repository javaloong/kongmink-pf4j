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
package org.javaloong.kongmink.pf4j.spring.boot;

import org.pf4j.ExtensionFactory;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Pf4j ExtensionFactory to create/retrieve extension bean from spring
 *
 * {@link org.springframework.context.ApplicationContext}
 * 
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SpringExtensionFactory implements ExtensionFactory {

    private SpringBootPluginManager pluginManager;

    public SpringExtensionFactory(SpringBootPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> extensionClass) {
        GenericApplicationContext pluginApplicationContext = getApplicationContext(extensionClass);
        Object extension = null;
        try {
            extension = pluginApplicationContext.getBean(extensionClass);
        } catch (NoSuchBeanDefinitionException ignored) {} // do nothing
        if (extension == null) {
            Object extensionBean = createWithoutSpring(extensionClass);
            pluginApplicationContext.getBeanFactory().registerSingleton(
                    extensionClass.getName(), extensionBean);
            extension = extensionBean;
        }
        //no inspection unchecked
        return (T) extension;
    }

    public String getExtensionBeanName(Class<?> extensionClass) {
        ApplicationContext pluginAppCtx = getApplicationContext(extensionClass);
        if (pluginAppCtx == null) return null;
        String[] beanNames = pluginAppCtx.getBeanNamesForType(extensionClass);
        return beanNames.length > 0 ? beanNames[0] : null;
    }

    private Object createWithoutSpring(Class<?> extensionClass) {
        try {
            return extensionClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private GenericApplicationContext getApplicationContext(Class<?> extensionClass) {
        PluginWrapper pluginWrapper = pluginManager.whichPlugin(extensionClass);
        SpringBootPlugin plugin = (SpringBootPlugin) pluginWrapper.getPlugin();
        return plugin.getApplicationContext();
    }
}
