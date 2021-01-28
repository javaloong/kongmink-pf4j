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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.javaloong.kongmink.pf4j.spring.boot.model.UpdatePluginInfo;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.UpdateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Xu Cheng
 */
@RestController
@RequestMapping(value = "${spring.pf4j.controller.base-path:/pf4j}/updates")
public class UpdateManagerController {

    @Autowired
    private UpdateManager updateManager;

    @Autowired
    private PluginManager pluginManager;
    
    @GetMapping(value = "/available-plugins")
    public List<PluginInfo> getAvailablePlugins() {
        return updateManager.getAvailablePlugins();
    }
    
    @GetMapping(value = "/plugins")
    public List<UpdatePluginInfo> getPlugins() {
        List<PluginInfo> plugins = updateManager.getPlugins();
        List<UpdatePluginInfo> updatePlugins = new ArrayList<>(plugins.size());
        for (PluginInfo info : plugins) {
            PluginWrapper wrapper = pluginManager.getPlugin(info.id);
            PluginState state = wrapper != null ? wrapper.getPluginState() : null;
            UpdatePluginInfo updateInfo = UpdatePluginInfo.build(info, 
                    state != null ? state.name() : "UNINSTALLED");
            updatePlugins.add(updateInfo);
        }
        return updatePlugins;
    }
    
    @PostMapping(value = "/install/{pluginId}/{version}")
    public Object install(@PathVariable("pluginId") String pluginId, @PathVariable("version") String version) {
        boolean result = updateManager.installPlugin(pluginId, version);
        return Collections.singletonMap("result", result);
    }

    @PostMapping(value = "/uninstall/{pluginId}")
    public Object uninstall(@PathVariable("pluginId") String pluginId) {
        boolean result = updateManager.uninstallPlugin(pluginId);
        return Collections.singletonMap("result", result);
    }
}
