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
package org.javaloong.kongmink.pf4j.spring.boot.env;

import java.util.Map;

import org.pf4j.PluginRuntimeException;

/**
 * Contract for configuration repositories.
 * 
 * @author Xu Cheng
 */
public interface ConfigurationRepository {

    /**
     * Get a particular plugin configuration properties from this repository.
     *
     * @param id the id of the plugin
     * @return the plugin configuration properties
     */
    Map<String, Object> get(String id);
    
    /**
     * Save a plugin configuration properties in this repository.
     * 
     * @param id the id of the plugin
     * @param properties the configuration properties of the plugin
     */
    void save(String id, Map<String, Object> properties);
    
    /**
     * Removes a plugin configuration properties from the repository.
     *
     * @param id the id of the plugin
     * @return true if deleted
     * @throws PluginRuntimeException if something goes wrong
     */
    boolean delete(String id);
}
