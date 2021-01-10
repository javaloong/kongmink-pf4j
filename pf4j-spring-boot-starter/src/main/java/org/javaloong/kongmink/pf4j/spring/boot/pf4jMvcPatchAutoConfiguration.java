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

import org.javaloong.kongmink.pf4j.spring.boot.context.PluginStateChangedEvent;
import org.javaloong.kongmink.pf4j.spring.boot.web.servlet.PluginRequestMappingHandlerMapping;
import org.pf4j.PluginManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.PluginResourceHandlerRegistrationCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * pf4j main application auto configuration for Spring Boot
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 * @see pf4jProperties
 */
@Configuration
@ConditionalOnClass({ PluginManager.class, SpringBootPluginManager.class })
@ConditionalOnProperty(prefix = pf4jProperties.PREFIX, value = "enabled", havingValue = "true")
public class pf4jMvcPatchAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(WebMvcRegistrations.class)
	public WebMvcRegistrations mvcRegistrations() {
		return new WebMvcRegistrations() {
			@Override
			public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
				return new PluginRequestMappingHandlerMapping();
			}

			@Override
			public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
				return null;
			}

			@Override
			public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver() {
				return null;
			}
		};
	}

	@Bean
	public PluginResourceHandlerRegistrationCustomizer resourceHandlerRegistrationCustomizer() {
		return new PluginResourceHandlerRegistrationCustomizer();
	}

	@EventListener(PluginStateChangedEvent.class)
	public void onPluginStarted() {

	}
}