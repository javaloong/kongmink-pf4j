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

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.MongoDatabaseFactory;

/**
 * Demonstrate how to share {@link MongoDatabaseFactory} from main {@link ApplicationContext},
 * so plugin could use the same database as main application and share database connection resource,
 * e.g. connection pool, transaction, etc.
 *
 * <p>Note that related AutoConfigurations have to be excluded explicitly to avoid
 * duplicated resource retaining.
 * 
 * @author Xu Cheng
 */
public class SharedMongoDatabaseSpringBootstrap extends SpringBootstrap {

    public SharedMongoDatabaseSpringBootstrap(SpringBootPlugin plugin, Class<?>... primarySources) {
        super(plugin, primarySources);
    }

    @Override
    protected String[] getExcludeConfigurations() {
        return ArrayUtils.addAll(super.getExcludeConfigurations(),
                "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration");
    }

    @Override
    public ConfigurableApplicationContext createApplicationContext() {
        AnnotationConfigApplicationContext applicationContext =
                (AnnotationConfigApplicationContext) super.createApplicationContext();
        // share MongoDatabaseFactory
        importBeanFromMainContext(applicationContext, "mongoDatabaseFactory");

        return applicationContext;
    }
    
}
