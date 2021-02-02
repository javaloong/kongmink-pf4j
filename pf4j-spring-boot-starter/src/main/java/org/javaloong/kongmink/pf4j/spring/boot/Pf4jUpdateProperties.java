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

import java.net.URL;
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
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for pf4j main application
 * @author Xu Cheng
 * @see Pf4jUpdateAutoConfiguration
 */
@ConfigurationProperties(prefix = Pf4jUpdateProperties.PREFIX)
public class Pf4jUpdateProperties {

    public static final String PREFIX = "spring.pf4j.update";
    
    /** 
     * Enable Pf4j Update. 
     */
    private boolean enabled = false;
    /** 
     * Local Repositories Path , i.e : repositories.json
     */
    private String repositoriesJsonPath;
    /** 
     * Remote Repositories Path 
     */
    private List<Pf4jUpdateRepository> repositories = new ArrayList<Pf4jUpdateRepository>();
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRepositoriesJsonPath() {
        return repositoriesJsonPath;
    }

    public void setRepositoriesJsonPath(String repositoriesJsonPath) {
        this.repositoriesJsonPath = repositoriesJsonPath;
    }
    
    public List<Pf4jUpdateRepository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Pf4jUpdateRepository> repositories) {
        this.repositories = repositories;
    }

    public static class Pf4jUpdateRepository {
        
        /**
         * Repository ID
         */
        private String id;
        /**
         * Repository URL
         */
        private URL url;
        /**
         * Repository plugins JSON file name
         */
        private String pluginsJsonFileName = "plugins.json";

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public URL getUrl() {
            return url;
        }

        public void setUrl(URL url) {
            this.url = url;
        }

        public String getPluginsJsonFileName() {
            return pluginsJsonFileName;
        }

        public void setPluginsJsonFileName(String pluginsJsonFileName) {
            this.pluginsJsonFileName = pluginsJsonFileName;
        }
    }
}
