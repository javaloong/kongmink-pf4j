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
package org.javaloong.kongmink.pf4j.spring.boot.model;

import org.javaloong.kongmink.pf4j.spring.boot.context.PluginStartingError;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class PluginInfo implements PluginDescriptor {

    public String pluginId;

    public String pluginDescription;

    public String pluginClass;

    public String version;

    public String requires;

    public String provider;

    public String license;

    public List<PluginDependency> dependencies;

    public PluginState pluginState;

    public String newVersion;

    public boolean removed;

    public PluginStartingError startingError;

    public String getPluginId() {
        return pluginId;
    }

    public String getPluginDescription() {
        return pluginDescription;
    }

    public String getPluginClass() {
        return pluginClass;
    }

    public String getVersion() {
        return version;
    }

    public String getRequires() {
        return requires;
    }

    public String getProvider() {
        return provider;
    }

    public String getLicense() {
        return license;
    }

    public List<PluginDependency> getDependencies() {
        return dependencies;
    }

    public PluginState getPluginState() {
        return pluginState;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public boolean isRemoved() {
        return removed;
    }

    public PluginStartingError getStartingError() {
        return startingError;
    }

    public static PluginInfo build(PluginDescriptor descriptor,
                                   PluginState pluginState,
                                   String newVersion,
                                   PluginStartingError startingError,
                                   boolean removed) {
        PluginInfo pluginInfo = new PluginInfo();
        pluginInfo.pluginId = descriptor.getPluginId();
        pluginInfo.pluginDescription = descriptor.getPluginDescription();
        pluginInfo.pluginClass = descriptor.getPluginClass();
        pluginInfo.version = descriptor.getVersion();
        pluginInfo.requires = descriptor.getRequires();
        pluginInfo.provider = descriptor.getProvider();
        pluginInfo.license = descriptor.getLicense();
        if (descriptor.getDependencies() != null) {
            pluginInfo.dependencies = new ArrayList<>(descriptor.getDependencies());
        }
        pluginInfo.pluginState = pluginState;
        pluginInfo.startingError = startingError;
        pluginInfo.newVersion = newVersion;
        pluginInfo.removed = removed;
        return pluginInfo;
    }
}
