package org.javaloong.kongmink.pf4j.spring.boot.env;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

public class DefaultConfigurationRepositoryTest {

    private Path configRoot;
    private ConfigurationRepository repository;
    
    @BeforeEach
    void init() {
        File configDir = new File("build", "config");
        configRoot = Paths.get(configDir.getAbsolutePath());
        repository = new DefaultConfigurationRepository(configRoot);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        FileSystemUtils.deleteRecursively(configRoot);
    }
    
    @Test
    public void get_FileNotFound_ShouldReturnEmptyConfigProperties() throws Exception {
        Map<String, Object> properties = repository.get("plugin1");
        assertTrue(properties.isEmpty());
    }
    
    @Test
    public void get_FileFound_ShouldReturnConfigProperties() throws Exception {
        saveFile("plugin1");
        Map<String, Object> properties = repository.get("plugin1");
        assertThat(properties.get("key1"), is("value1"));
        assertThat(properties.get("key2"), is("value2"));
    }
    
    @Test
    public void save_ShouldAddConfigProperties() throws Exception {
        saveFile("plugin1");
        Path propertiesPath = configRoot.resolve("plugin1.properties");
        assertTrue(Files.exists(propertiesPath));
    }
    
    @Test
    public void delete_ShouldRemoveConfigProperties() throws Exception {
        saveFile("plugin1");
        assertTrue(repository.delete("plugin1"));
    }
    
    private void saveFile(String pluginId) {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        repository.save(pluginId, map);
    }
}
