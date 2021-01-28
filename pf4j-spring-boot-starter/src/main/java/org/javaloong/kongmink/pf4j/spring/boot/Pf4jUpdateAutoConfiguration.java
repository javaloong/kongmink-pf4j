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

import java.nio.file.Paths;

import org.javaloong.kongmink.pf4j.spring.boot.Pf4jUpdateProperties.Pf4jUpdateRepository;
import org.pf4j.PluginManager;
import org.pf4j.update.DefaultUpdateRepository;
import org.pf4j.update.UpdateManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * pf4j main application auto configuration update for Spring Boot
 * @author Xu Cheng
 * @see Pf4jUpdateProperties
 */
@Configuration
@AutoConfigureAfter({ Pf4jAutoConfiguration.class })
@ConditionalOnClass({ UpdateManager.class })
@ConditionalOnProperty(prefix = Pf4jUpdateProperties.PREFIX, value = "enabled", havingValue = "true")
@EnableConfigurationProperties({Pf4jUpdateProperties.class})
public class Pf4jUpdateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(UpdateManagerController.class)
    @ConditionalOnProperty(name = "spring.pf4j.controller.base-path")
    public UpdateManagerController updateManagerController() {
        return new UpdateManagerController();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public UpdateManager updateManager(PluginManager pluginManager, Pf4jUpdateProperties properties) {
        UpdateManager updateManager = null;
        if (StringUtils.hasText(properties.getRepositoriesJsonPath())) {
            updateManager = new UpdateManager(pluginManager, Paths.get(properties.getRepositoriesJsonPath()));
        } 
        else {
            updateManager = new UpdateManager(pluginManager);
        }
        if(!CollectionUtils.isEmpty(properties.getRepositories())) {
            for (Pf4jUpdateRepository repo : properties.getRepositories()) {
                updateManager.addRepository(new DefaultUpdateRepository(repo.getId(), 
                        repo.getUrl(), repo.getPluginsJsonFileName()));
            }
        }
        return updateManager;
    }
}
