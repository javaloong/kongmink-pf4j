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

import static org.pf4j.AbstractPluginManager.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

import org.javaloong.kongmink.pf4j.spring.boot.context.MainApplicationReadyListener;
import org.javaloong.kongmink.pf4j.spring.boot.context.MainApplicationStartedListener;
import org.pf4j.CompoundPluginLoader;
import org.pf4j.DevelopmentPluginLoader;
import org.pf4j.JarPluginLoader;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginLoader;
import org.pf4j.PluginManager;
import org.pf4j.PluginStateListener;
import org.pf4j.PluginStatusProvider;
import org.pf4j.RuntimeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * pf4j main application auto configuration for Spring Boot
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 * @see Pf4jProperties
 */
@Configuration
@ConditionalOnClass({ PluginManager.class, SpringBootPluginManager.class })
@ConditionalOnProperty(prefix = Pf4jProperties.PREFIX, value = "enabled", havingValue = "true")
@EnableConfigurationProperties({Pf4jProperties.class, Pf4jPluginProperties.class})
@Import({MainApplicationStartedListener.class, MainApplicationReadyListener.class})
public class Pf4jAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(Pf4jAutoConfiguration.class);
    
	@Bean
	@ConditionalOnMissingBean(PluginStateListener.class)
	public PluginStateListener pluginStateListener() {
		return event -> {
			PluginDescriptor descriptor = event.getPlugin().getDescriptor();
			if (log.isDebugEnabled()) {
				log.debug("Plugin [{}（{}）]({}) {}", descriptor.getPluginId(),
						descriptor.getVersion(), descriptor.getPluginDescription(),
						event.getPluginState().toString());
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(PluginManagerController.class)
	@ConditionalOnProperty(name = "spring.pf4j.controller.base-path")
	public PluginManagerController pluginManagerController() {
		return new PluginManagerController();
	}

	@Bean
	@ConditionalOnMissingBean
	public SpringBootPluginManager pluginManager(Pf4jProperties properties) {
		// Setup RuntimeMode
		System.setProperty(MODE_PROPERTY_NAME, properties.getRuntimeMode().toString());

		// Setup Plugin folder
		String pluginsRoot = (RuntimeMode.DEPLOYMENT == properties.getRuntimeMode()) ? 
		        DEFAULT_PLUGINS_DIR : DEVELOPMENT_PLUGINS_DIR;
		if(StringUtils.hasText(properties.getPluginsRoot())) {
		    pluginsRoot = properties.getPluginsRoot();
		}
		System.setProperty(PLUGINS_DIR_PROPERTY_NAME, pluginsRoot);
		String appHome = System.getProperty("app.home");
		if (RuntimeMode.DEPLOYMENT == properties.getRuntimeMode()
				&& StringUtils.hasText(appHome)) {
			System.setProperty(PLUGINS_DIR_PROPERTY_NAME, appHome + File.separator + pluginsRoot);
		}

		SpringBootPluginManager pluginManager = new SpringBootPluginManager() {
			@Override
			protected PluginLoader createPluginLoader() {
				if (properties.getCustomPluginLoader() != null) {
					Class<PluginLoader> clazz = properties.getCustomPluginLoader();
					try {
						Constructor<?> constructor = clazz.getConstructor(PluginManager.class);
						return (PluginLoader) constructor.newInstance(this);
					} catch (Exception ex) {
						throw new IllegalArgumentException(String.format("Create custom PluginLoader %s failed. Make sure" +
								"there is a constructor with one argument that accepts PluginLoader", clazz.getName()));
					}
				} else {
					return new CompoundPluginLoader()
							.add(new DevelopmentPluginLoader(this) {
								@Override
								protected PluginClassLoader createPluginClassLoader(Path pluginPath,
																					PluginDescriptor pluginDescriptor) {
									if (properties.getClassesDirectories() != null && properties.getClassesDirectories().size() > 0) {
										for (String classesDirectory : properties.getClassesDirectories()) {
											pluginClasspath.addClassesDirectories(classesDirectory);
										}
									}
									if (properties.getLibDirectories() != null && properties.getLibDirectories().size() > 0) {
										for (String libDirectory : properties.getLibDirectories()) {
											pluginClasspath.addJarsDirectories(libDirectory);
										}
									}
									return new SpringBootPluginClassLoader(pluginManager,
											pluginDescriptor, getClass().getClassLoader());
								}
							}, this::isDevelopment)
							.add(new JarPluginLoader(this) {
								@Override
								public ClassLoader loadPlugin(Path pluginPath, PluginDescriptor pluginDescriptor) {
									PluginClassLoader pluginClassLoader = new SpringBootPluginClassLoader(pluginManager, pluginDescriptor, getClass().getClassLoader());
									pluginClassLoader.addFile(pluginPath.toFile());
									return pluginClassLoader;
								}
							}, this::isNotDevelopment);
				}
			}

			@Override
			protected PluginStatusProvider createPluginStatusProvider() {
				if (PropertyPluginStatusProvider.isPropertySet(properties)) {
					return new PropertyPluginStatusProvider(properties);
				}
				return super.createPluginStatusProvider();
			}
		};

		pluginManager.setAutoStartPlugin(properties.isAutoStartPlugin());
		pluginManager.setProfiles(properties.getPluginProfiles());
		pluginManager.presetProperties(flatProperties(properties.getPluginProperties()));
		pluginManager.setExactVersionAllowed(properties.isExactVersionAllowed());
		pluginManager.setSystemVersion(properties.getSystemVersion());

		return pluginManager;
	}

	private Map<String, Object> flatProperties(Map<String, Object> propertiesMap) {
		Stack<String> pathStack = new Stack<>();
		Map<String, Object> flatMap = new HashMap<>();
		propertiesMap.entrySet().forEach(mapEntry -> {
			recurse(mapEntry, entry -> {
				pathStack.push(entry.getKey());
				if (entry.getValue() instanceof Map) return;
				flatMap.put(String.join(".", pathStack), entry.getValue());

			}, entry -> {
				pathStack.pop();
			});
		});
		return flatMap;
	}

	@SuppressWarnings("unchecked")
    private void recurse(Map.Entry<String, Object> entry,
						 Consumer<Map.Entry<String, Object>> preConsumer,
						 Consumer<Map.Entry<String, Object>> postConsumer) {
		preConsumer.accept(entry);

		if (entry.getValue() instanceof Map) {
			Map<String, Object> entryMap = (Map<String, Object>) entry.getValue();
			for (Map.Entry<String, Object> subEntry : entryMap.entrySet()) {
				recurse(subEntry, preConsumer, postConsumer);
			}
		}

		postConsumer.accept(entry);
	}

}
