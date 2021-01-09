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
package org.javaloong.kongmink.pf4j.spring.boot.web.servlet;

import org.javaloong.kongmink.pf4j.spring.boot.SpringBootPlugin;
import org.javaloong.kongmink.pf4j.spring.boot.SpringBootstrap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class PluginRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    /**
     * {@inheritDoc}
     */
    @Override
    public void detectHandlerMethods(Object controller) {
        super.detectHandlerMethods(controller);
    }

    public void registerControllers(SpringBootPlugin springBootPlugin) {
        getControllerBeans(springBootPlugin).forEach(bean -> registerController(springBootPlugin, bean));
    }

    private void registerController(SpringBootPlugin springBootPlugin, Object controller) {
        String beanName = controller.getClass().getName();
        // unregister RequestMapping if already registered
        unregisterController(springBootPlugin.getMainApplicationContext(), controller);
        springBootPlugin.registerBeanToMainContext(beanName, controller);
        detectHandlerMethods(controller);
    }

    public void unregisterControllers(SpringBootPlugin springBootPlugin) {
        getControllerBeans(springBootPlugin).forEach(bean ->
                unregisterController(springBootPlugin.getMainApplicationContext(), bean));
    }

    @SuppressWarnings("unchecked")
    public Set<Object> getControllerBeans(SpringBootPlugin springBootPlugin) {
        LinkedHashSet<Object> beans = new LinkedHashSet<>();
        ApplicationContext applicationContext = springBootPlugin.getApplicationContext();
        //no inspection unchecked
        Set<String> sharedBeanNames = (Set<String>) applicationContext.getBean(
                SpringBootstrap.BEAN_IMPORTED_BEAN_NAMES);
        beans.addAll(applicationContext.getBeansWithAnnotation(Controller.class)
                .entrySet().stream().filter(beanEntry -> !sharedBeanNames.contains(beanEntry.getKey()))
                .map(Map.Entry::getValue).collect(Collectors.toList()));
        beans.addAll(applicationContext.getBeansWithAnnotation(RestController.class)
                .entrySet().stream().filter(beanEntry -> !sharedBeanNames.contains(beanEntry.getKey()))
                .map(Map.Entry::getValue).collect(Collectors.toList()));
        return beans;
    }

    public void unregisterController(GenericApplicationContext mainCtx, Object controller) {
        new HashMap<>(getHandlerMethods()).forEach((mapping, handlerMethod) -> {
            if (controller == handlerMethod.getBean()) super.unregisterMapping(mapping);
        });
        SpringBootPlugin.unregisterBeanFromMainContext(mainCtx, controller);
    }

}
