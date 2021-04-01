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

import javax.sql.DataSource;

/**
 * Demonstrate how to share {@link DataSource} from main {@link ApplicationContext},
 * so plugin could use the same database as main application and share database connection resource,
 * e.g. connection pool, transaction, etc.
 *
 * <p>Note that related AutoConfigurations have to be excluded explicitly to avoid
 * duplicated resource retaining.
 * 
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 * @author Xu Cheng
 */
public class SharedDataSourceSpringBootstrap extends SpringBootstrap {

    private boolean tmShared = false;
    
    public SharedDataSourceSpringBootstrap(SpringBootPlugin plugin, Class<?>... primarySources) {
        super(plugin, primarySources);
    }
    
    /**
     * Transaction manager bean that wanted to be shared from main {@link ApplicationContext}.
     * Note that this method only takes effect before {@link #run(String...)} method.
     * @return the current bootstrap
     */
    public SharedDataSourceSpringBootstrap importTransactionManager() {
        this.tmShared = true;
        return this;
    }

    @Override
    protected String[] getExcludeConfigurations() {
        return ArrayUtils.addAll(super.getExcludeConfigurations(),
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
                "org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration");
    }

    @Override
    public ConfigurableApplicationContext createApplicationContext() {
        AnnotationConfigApplicationContext applicationContext =
                (AnnotationConfigApplicationContext) super.createApplicationContext();
        // share dataSource
        importBeanFromMainContext(applicationContext, DataSource.class);
        if(tmShared) {
            importBeanFromMainContext(applicationContext, "transactionManager");
        }
        return applicationContext;
    }

}
