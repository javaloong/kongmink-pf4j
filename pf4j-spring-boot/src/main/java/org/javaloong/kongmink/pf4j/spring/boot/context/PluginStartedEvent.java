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
package org.javaloong.kongmink.pf4j.spring.boot.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * This event will be published to <b>plugin application context</b> once plugin application context is started.
 * 
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class PluginStartedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1651490578605729784L;

    public PluginStartedEvent(ApplicationContext pluginApplicationContext) {
        super(pluginApplicationContext);
    }
}
