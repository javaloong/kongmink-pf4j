package org.javaloong.kongmink.pf4j.spring.boot.test;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.javaloong.kongmink.pf4j.spring.boot.PluginManagerController;
import org.javaloong.kongmink.pf4j.spring.boot.SpringBootPluginManager;
import org.junit.jupiter.api.Test;
import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginRepository;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PluginManagerController.class)
public class PluginManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SpringBootPluginManager pluginManager;
    
    @Test
    public void list_ShouldReturnPluginInfoItems() throws Exception{
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        PluginDescriptorFinder pluginDescriptorFinder = mock(PluginDescriptorFinder.class);
        PluginRepository pluginRepository = mock(PluginRepository.class);
        PluginDescriptor pluginDescriptor = new DefaultPluginDescriptor(
                "plugin1", "plugin1 description", "plugin1Class", "1.0.0", null, null, null);
        when(pluginManager.getPlugins()).thenReturn(Arrays.asList(pluginWrapper));
        when(pluginManager.getPluginDescriptorFinder()).thenReturn(pluginDescriptorFinder);
        when(pluginManager.getPluginRepository()).thenReturn(pluginRepository);
        when(pluginWrapper.getDescriptor()).thenReturn(pluginDescriptor);
        when(pluginDescriptorFinder.find(any())).thenReturn(null);
        when(pluginRepository.getPluginPaths()).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/plugins"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].pluginId", is("plugin1")));
    }
    
    @Test
    public void start_ShouldReturnHttpStatusOkAndPluginStateStarted() throws Exception{
        when(pluginManager.startPlugin(anyString())).thenReturn(PluginState.STARTED);
        
        mockMvc.perform(post("/api/plugins/plugin1/start"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state", is("STARTED")));
    }
    
    @Test
    public void stop_ShouldReturnHttpStatusOkAndPluginStateStopped() throws Exception{
        when(pluginManager.stopPlugin(anyString())).thenReturn(PluginState.STOPPED);
        
        mockMvc.perform(post("/api/plugins/plugin1/stop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state", is("STOPPED")));
    }
    
    @Test
    public void reload_ShouldReturnHttpStatusOkAndPluginStateStarted() throws Exception{
        when(pluginManager.reloadPlugins(anyString())).thenReturn(PluginState.STARTED);
        
        mockMvc.perform(post("/api/plugins/plugin1/reload"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state", is("STARTED")));
    }
    
    @Test
    public void reloadAll_ShouldReturnHttpStatusOk() throws Exception{
        doNothing().when(pluginManager).reloadPlugins(false);
        
        mockMvc.perform(post("/api/plugins/reload"))
            .andExpect(status().isOk());
    }
    
    @TestConfiguration
    @Import(PluginManagerController.class)
    static class Config {
        
    }
}
