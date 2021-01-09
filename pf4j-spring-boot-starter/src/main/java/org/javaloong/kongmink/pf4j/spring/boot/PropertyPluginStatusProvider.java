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

import org.pf4j.PluginStatusProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class PropertyPluginStatusProvider implements PluginStatusProvider {

    private List<String> enabledPlugins;
    private List<String> disabledPlugins;

    public PropertyPluginStatusProvider(pf4jProperties properties) {
        this.enabledPlugins = properties.getEnabledPlugins() != null
                ? Arrays.asList(properties.getEnabledPlugins()) : new ArrayList<>();
        this.disabledPlugins = properties.getDisabledPlugins() != null
                ? Arrays.asList(properties.getDisabledPlugins()) : new ArrayList<>();
    }

    public static boolean isPropertySet(pf4jProperties properties) {
        return properties.getEnabledPlugins() != null && properties.getEnabledPlugins().length > 0
                || properties.getDisabledPlugins() != null && properties.getDisabledPlugins().length > 0;
    }

    @Override
    public boolean isPluginDisabled(String pluginId) {
        if (disabledPlugins.contains(pluginId)) return true;
        return !enabledPlugins.isEmpty() && !enabledPlugins.contains(pluginId);
    }

    @Override
    public void disablePlugin(String pluginId) {
        if (isPluginDisabled(pluginId)) return;
        disabledPlugins.add(pluginId);
        enabledPlugins.remove(pluginId);
    }

    @Override
    public void enablePlugin(String pluginId) {
        if (!isPluginDisabled(pluginId)) return;
        enabledPlugins.add(pluginId);
        disabledPlugins.remove(pluginId);
    }
}
