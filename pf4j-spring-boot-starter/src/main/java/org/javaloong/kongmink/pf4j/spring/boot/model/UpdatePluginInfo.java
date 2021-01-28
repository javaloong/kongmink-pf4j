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

import org.pf4j.update.PluginInfo;

/**
 * @author Xu Cheng
 */
public class UpdatePluginInfo extends PluginInfo {

    private static final long serialVersionUID = 990777157219144364L;

    public String state;
    
    public static UpdatePluginInfo build(PluginInfo info, String state) {
        UpdatePluginInfo updateInfo = new UpdatePluginInfo();
        updateInfo.id = info.id;
        updateInfo.name = info.name;
        updateInfo.description = info.description;
        updateInfo.provider = info.provider;
        updateInfo.projectUrl = info.projectUrl;
        updateInfo.releases = info.releases;
        updateInfo.state = state;
        return updateInfo;
    }
}
