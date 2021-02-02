package org.javaloong.kongmink.pf4j.spring.boot.test;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.javaloong.kongmink.pf4j.spring.boot.UpdateManagerController;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.PluginInfo.PluginRelease;
import org.pf4j.update.UpdateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UpdateManagerController.class)
public class UpdateManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UpdateManager updateManager;
    
    @MockBean
    private PluginManager pluginManager;
    
    @Test
    public void getAvailablePlugins_ShouldReturnPluginInfoItems() throws Exception{
        when(updateManager.getAvailablePlugins()).thenReturn(createPlugins());
        
        mockMvc.perform(get("/api/updates/available-plugins"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is("plugin1")));
    }
    
    @Test
    public void getPlugins_ShouldReturnUpdatePluginInfoItems() throws Exception{
        when(updateManager.getPlugins()).thenReturn(createPlugins());
        when(pluginManager.getPlugin(anyString())).thenReturn(mock(PluginWrapper.class));
        
        mockMvc.perform(get("/api/updates/plugins"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is("plugin1")))
            .andExpect(jsonPath("$[0].state", is("UNINSTALLED")));
    }
    
    @Test
    public void install_ShouldReturnHttpStatusOk() throws Exception{
        when(updateManager.installPlugin(anyString(), anyString())).thenReturn(true);
        
        mockMvc.perform(post("/api/updates/install/plugin1/1.0.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is(true)));
    }
    
    @Test
    public void uninstall_ShouldReturnHttpStatusOk() throws Exception{
        when(updateManager.uninstallPlugin(anyString())).thenReturn(true);
        
        mockMvc.perform(post("/api/updates/uninstall/plugin1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is(true)));
    }
    
    private List<PluginInfo> createPlugins(){
        PluginInfo info = new PluginInfo();
        info.id = "plugin1";
        info.name = "plugin1";
        PluginRelease release = new PluginRelease();
        release.version = "1.0.0";
        info.releases = Arrays.asList(release);
        return Arrays.asList(info);
    }
    
    @TestConfiguration
    @Import(UpdateManagerController.class)
    static class Config {
        
    }
}
