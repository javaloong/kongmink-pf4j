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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import org.pf4j.PluginRuntimeException;
import org.pf4j.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Xu Cheng
 */
public class DefaultConfigurationRepository implements ConfigurationRepository {

    private static final Logger log = LoggerFactory.getLogger(DefaultConfigurationRepository.class);
    
    public static final String CONFIG_FILE_EXTENSION = ".properties";
    
    private final Path configRoot;
    
    public DefaultConfigurationRepository(Path configRoot) {
        this.configRoot = configRoot;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Map<String, Object> get(String id) {
        String propertiesFileName = getPropertiesFileName(id);
        return (Map)readProperties(configRoot, propertiesFileName);
    }

    @Override
    public void save(String id, Map<String, Object> properties) {
        String propertiesFileName = getPropertiesFileName(id);
        writeProperties(configRoot, propertiesFileName, properties);
    }
    
    @Override
    public boolean delete(String id) {
        String propertiesFileName = getPropertiesFileName(id);
        return deleteProperties(configRoot, propertiesFileName);
    }
    
    protected Properties readProperties(Path configPath, String propertiesFileName) {
        Path propertiesPath = getPropertiesPath(configPath, propertiesFileName);
        
        Properties properties = new Properties();
        try {
            log.debug("Lookup plugin configuration properties in '{}'", propertiesPath);
            if (Files.notExists(propertiesPath)) {
                return properties;
            }

            try (InputStream input = Files.newInputStream(propertiesPath)) {
                properties.load(input);
            } catch (IOException e) {
                throw new PluginRuntimeException(e);
            }
        } finally {
            FileUtils.closePath(propertiesPath);
        }
        
        return properties;
    }
    
    protected void writeProperties(Path configPath, String propertiesFileName, Map<String, ?> map) {
        Path propertiesPath = getPropertiesPath(configPath, propertiesFileName);
        
        try {
            log.debug("Store plugin configuration properties in '{}'", propertiesPath);
            if (Files.notExists(configPath)) {
                Files.createDirectories(configPath);
            }

            try (OutputStream output = Files.newOutputStream(propertiesPath)) {
                Properties properties = new Properties();
                properties.putAll(map);
                properties.store(output, null);
            }
        } catch (IOException e) {
            throw new PluginRuntimeException(e);
        } finally {
            FileUtils.closePath(propertiesPath);
        }
    }
    
    protected boolean deleteProperties(Path configPath, String propertiesFileName) {
        Path propertiesPath = getPropertiesPath(configRoot, propertiesFileName);
        
        try {
            log.debug("Delete plugin configuration properties in '{}'", propertiesPath);
            
            FileUtils.delete(propertiesPath);
            return true;
        } catch (NoSuchFileException e) {
            return false; 
        } catch (IOException e) {
            throw new PluginRuntimeException(e);
        }
    }
    
    protected String getPropertiesFileName(String pluginId) {
        return pluginId.toLowerCase() + CONFIG_FILE_EXTENSION;
    }
    
    protected Path getPropertiesPath(Path configPath, String propertiesFileName) {
        return configPath.resolve(Paths.get(propertiesFileName));
    }
}
