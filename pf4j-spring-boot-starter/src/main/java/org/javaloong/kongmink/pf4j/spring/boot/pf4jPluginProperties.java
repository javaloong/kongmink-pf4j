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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for Spring boot plugin
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 * @see pf4jAutoConfiguration
 */
@ConfigurationProperties(prefix = pf4jPluginProperties.PREFIX)
public class pf4jPluginProperties {

	public static final String PREFIX = "pf4j-plugin";

	/**
	 * Load these classes from plugin classpath first,
	 * e.g Spring Boot AutoConfiguration used in plugin only.
	 */
	public String[] pluginFirstClasses = {};
	/**
	 * Load these resource from plugin classpath only
	 */
	public String[] pluginOnlyResources = {};
	
    public String[] getPluginFirstClasses() {
        return pluginFirstClasses;
    }
    
    public void setPluginFirstClasses(String[] pluginFirstClasses) {
        this.pluginFirstClasses = pluginFirstClasses;
    }
    
    public String[] getPluginOnlyResources() {
        return pluginOnlyResources;
    }
    
    public void setPluginOnlyResources(String[] pluginOnlyResources) {
        this.pluginOnlyResources = pluginOnlyResources;
    }
}
