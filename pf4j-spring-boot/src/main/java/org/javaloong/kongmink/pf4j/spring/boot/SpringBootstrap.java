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
import org.apache.commons.lang3.StringUtils;
import org.javaloong.kongmink.pf4j.spring.boot.env.ConfigurationRepository;
import org.pf4j.PluginDependency;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base plugin {@link ApplicationContext} bootstrap class like {@link SpringApplication}
 * to initialize environment in spring-boot style.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 * @author Xu Cheng
 */
public class SpringBootstrap extends SpringApplication {

    private final static Logger log = LoggerFactory.getLogger(SpringBootstrap.class);

    public final static String BEAN_PLUGIN = "pf4j.plugin";
    public final static String BEAN_IMPORTED_BEAN_NAMES = "sharedBeanNames";

    private static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";

    public static final String[] DEFAULT_EXCLUDE_CONFIGURATIONS = {
            "org.javaloong.kongmink.pf4j.spring.boot.Pf4jAutoConfiguration",
            // Spring Web MVC
            "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration",
            "org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration",
            "org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration",
            "org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration",
            "org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration",
            // Actuator/JMX
            "org.springframework.boot.actuate.autoconfigure.amqp.RabbitHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.audit.AuditAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.audit.AuditEventsEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.cache.CachesEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.cassandra.CassandraHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.cassandra.CassandraReactiveHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.cloudfoundry.servlet.CloudFoundryActuatorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.cloudfoundry.reactive.ReactiveCloudFoundryActuatorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.context.properties.ConfigurationPropertiesReportEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.context.ShutdownEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.couchbase.CouchbaseHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.couchbase.CouchbaseReactiveHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticSearchClientHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticSearchJestHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticSearchRestHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.endpoint.jmx.JmxEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.flyway.FlywayEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.influx.InfluxDbHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.integration.IntegrationGraphEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.jms.JmsHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.jolokia.JolokiaEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.ldap.LdapHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.liquibase.LiquibaseEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.logging.LogFileWebEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.logging.LoggersEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.management.HeapDumpWebEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.management.ThreadDumpEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.KafkaMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.Log4J2MetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.LogbackMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.amqp.RabbitMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.cache.CacheMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.appoptics.AppOpticsMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.atlas.AtlasMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.datadog.DatadogMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.dynatrace.DynatraceMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.elastic.ElasticMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.ganglia.GangliaMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.graphite.GraphiteMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.humio.HumioMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.influx.InfluxMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.jmx.JmxMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.kairos.KairosMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.newrelic.NewRelicMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.signalfx.SignalFxMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.statsd.StatsdMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.wavefront.WavefrontMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.jersey.JerseyServerMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.orm.jpa.HibernateMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.client.HttpClientMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.jetty.JettyMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.tomcat.TomcatMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.mongo.MongoHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.mongo.MongoReactiveHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.neo4j.Neo4jHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.redis.RedisHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.scheduling.ScheduledTasksEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.session.SessionsEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.solr.SolrHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.web.mappings.MappingsEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementContextAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextAutoConfiguration",
            "org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration",
            "org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration",
            // Spring Security
            "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration",
            "org.springframework.boot.autoconfigure.sendgrid.SendGridAutoConfiguration",
            "org.springframework.boot.autoconfigure.session.SessionAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration",
            // Spring Cloud
            "org.springframework.cloud.netflix.archaius.ArchaiusAutoConfiguration",
            "org.springframework.cloud.consul.config.ConsulConfigAutoConfiguration",
            "org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration",
            "org.springframework.cloud.autoconfigure.LifecycleMvcEndpointAutoConfiguration",
            "org.springframework.cloud.autoconfigure.RefreshAutoConfiguration",
            "org.springframework.cloud.autoconfigure.RefreshEndpointAutoConfiguration",
            "org.springframework.cloud.autoconfigure.WritableEnvironmentEndpointAutoConfiguration",
            "org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration",
            "org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration",
            "org.springframework.cloud.loadbalancer.config.LoadBalancerCacheAutoConfiguration",
            "org.springframework.cloud.consul.discovery.RibbonConsulAutoConfiguration",
            "org.springframework.cloud.consul.discovery.configclient.ConsulConfigServerAutoConfiguration",
            "org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration",
            "org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistryAutoConfiguration",
            "org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration",
            "org.springframework.cloud.consul.discovery.reactive.ConsulReactiveDiscoveryClientConfiguration",
            "org.springframework.cloud.consul.discovery.ConsulCatalogWatchAutoConfiguration",
            "org.springframework.cloud.consul.support.ConsulHeartbeatAutoConfiguration",
    };

    public static final String[] DEFAULT_EXCLUDE_APPLICATION_LISTENERS = {
            "org.springframework.cloud.bootstrap.BootstrapApplicationListener",
            "org.springframework.cloud.bootstrap.LoggingSystemShutdownListener",
            "org.springframework.cloud.context.restart.RestartListener",
    };

    private final SpringBootPlugin plugin;

    private final ApplicationContext mainApplicationContext;

    private final ClassLoader pluginClassLoader;

    private final HashSet<String> importBeanNames = new HashSet<>();
    
    private final HashSet<Class<?>> importBeanClasses = new HashSet<>();

    private final HashSet<String> importedBeanNames = new HashSet<>();

    private final Map<String, Object> presetProperties = new HashMap<>();

    private List<String> pluginFirstClasses;

    private List<String> pluginOnlyResources;
    
    private boolean pluginConfigEnabled;

    /**
     * Constructor should be the only thing need to take care for this Class.
     * Generally new an instance and {@link #run(String...)} it
     * in {@link SpringBootPlugin#createSpringBootstrap()} method.
     *
     * @param plugin the plugin for Spring Boot 
     * @param primarySources {@link SpringApplication} that annotated with @SpringBootApplication
     */
    public SpringBootstrap(SpringBootPlugin plugin, Class<?>... primarySources) {
        super(new DefaultResourceLoader(plugin.getWrapper().getPluginClassLoader()), primarySources);
        this.plugin = plugin;
        this.mainApplicationContext = plugin.getMainApplicationContext();
        this.pluginClassLoader = plugin.getWrapper().getPluginClassLoader();
        Map<String, Object> presetProperties = ((SpringBootPluginManager)
                plugin.getWrapper().getPluginManager()).getPresetProperties();
        if (presetProperties != null) this.presetProperties.putAll(presetProperties);
        this.presetProperties.put(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE,
                getExcludeConfigurations());
    }

    /**
     * Beans that wanted to be shared from main {@link ApplicationContext}.
     * Note that this method only takes effect before {@link #run(String...)} method.
     * @param beanName the bean name
     * @return the current bootstrap
     */
    public SpringBootstrap importBean(String beanName) {
        this.importBeanNames.add(beanName);
        return this;
    }

    /**
     * Beans that wanted to be shared from main {@link ApplicationContext}.
     * Note that this method only takes effect before {@link #run(String...)} method.
     * @param beanClass the bean class
     * @return the current bootstrap
     */
    public SpringBootstrap importBean(Class<?> beanClass) {
        this.importBeanClasses.add(beanClass);
        return this;
    }

    /**
     * Properties that need to be set when this application is started as a plugin.
     * Note that this method only takes effect before {@link #run(String...)} method.
     * @param name the property name
     * @param value the property value
     * @return the current bootstrap
     */
    public SpringBootstrap addPresetProperty(String name, Object value) {
        this.presetProperties.put(name, value);
        return this;
    }

    @Override
    protected void configurePropertySources(ConfigurableEnvironment environment,
                                            String[] args) {
        super.configurePropertySources(environment, args);
        String[] profiles = ((SpringBootPluginManager)
                plugin.getWrapper().getPluginManager()).getProfiles();
        if (!ArrayUtils.isEmpty(profiles)) environment.setActiveProfiles(profiles);
        environment.getPropertySources().addLast(new ExcludeConfigurations());
    }

    @Override
    protected void bindToSpringApplication(ConfigurableEnvironment environment) {
        super.bindToSpringApplication(environment);

        pluginFirstClasses = new ArrayList<>();
        String pluginFirstClassesProp = null;
        int i = 0;
        do {
            pluginFirstClassesProp = getProperties(environment, "pluginFirstClasses", i++);
            if (pluginFirstClassesProp != null) {
                pluginFirstClasses.add(pluginFirstClassesProp);
            }
        } while (pluginFirstClassesProp != null);

        pluginOnlyResources = new ArrayList<>();
        String pluginOnlyResourcesProp = null;
        i = 0;
        do {
            pluginOnlyResourcesProp = getProperties(environment, "pluginOnlyResources", i++);
            if (pluginOnlyResourcesProp != null) {
                pluginOnlyResources.add(pluginOnlyResourcesProp);
            }
        } while (pluginOnlyResourcesProp != null);
        
        pluginConfigEnabled = getProperties(environment, "pluginConfigEnabled", Boolean.class, false);
        // load external plugin configuration properties
        if (pluginConfigEnabled) {
            configurePluginPropertySources(environment);
        }
    }
    
    protected void configurePluginPropertySources(ConfigurableEnvironment environment) {
        String pluginId = plugin.getWrapper().getPluginId();
        ConfigurationRepository configurationRepository = ((SpringBootPluginManager)
            plugin.getWrapper().getPluginManager()).getConfigurationRepository();
        Map<String, Object> pluginProperties = configurationRepository.get(pluginId);
        if(!pluginProperties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(
                "Plugin Configurations", pluginProperties));
        }
    }

    /** 
     * Override this methods to customize excluded spring boot configuration
     * @return the exclude configurations
     */
    protected String[] getExcludeConfigurations() {
        return DEFAULT_EXCLUDE_CONFIGURATIONS;
    }

    /** 
     * Override this methods to customize excluded spring boot application listeners
     * @return the exclude application listeners
     */
    protected String[] getExcludeApplicationListeners() {
        return DEFAULT_EXCLUDE_APPLICATION_LISTENERS;
    }

    @Override
    public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
        super.setListeners(listeners
                .stream()
                .filter(listener -> !ArrayUtils.contains(
                        getExcludeApplicationListeners(), listener.getClass().getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public ConfigurableApplicationContext createApplicationContext() {
        setWebApplicationType(WebApplicationType.NONE);
        AnnotationConfigApplicationContext applicationContext =
                (AnnotationConfigApplicationContext) super.createApplicationContext();
//        applicationContext.setParent(mainApplicationContext);
        hackBeanFactory(applicationContext);
        applicationContext.setClassLoader(pluginClassLoader);

        applicationContext.getBeanFactory().registerSingleton(BEAN_PLUGIN, plugin);
        applicationContext.getBeanFactory().autowireBean(plugin);

        if (!CollectionUtils.isEmpty(importBeanNames)) {
            for (String beanName : importBeanNames) {
                // try to import bean from main applicationContext first
                boolean imported = importBeanFromMainContext(applicationContext, beanName);
                // not found, try to import bean from dependent applicationContext
                if (!imported) imported = importBeanFromDependentPlugin(applicationContext, beanName);
                if (!imported) log.error("Bean {} is not found", beanName);
            }
        }
        if (!CollectionUtils.isEmpty(importBeanClasses)) {
            for (Class<?> beanClass : importBeanClasses) {
                // try to import bean from main applicationContext first
                boolean imported = importBeanFromMainContext(applicationContext, beanClass);
                // not found, try to import bean from dependent applicationContext
                if (!imported) imported = importBeanFromDependentPlugin(applicationContext, beanClass);
                if (!imported) log.error("Bean {} is not found", beanClass);
            }
        }

        return applicationContext;
    }

    @Override
    protected void afterRefresh(ConfigurableApplicationContext context, ApplicationArguments args) {
        context.getBeanFactory().registerSingleton(BEAN_IMPORTED_BEAN_NAMES, importedBeanNames);
    }

    private void hackBeanFactory(ApplicationContext applicationContext) {
        if (pluginClassLoader instanceof SpringBootPluginClassLoader) {
            if (pluginFirstClasses != null) {
                ((SpringBootPluginClassLoader) pluginClassLoader).setPluginFirstClasses(pluginFirstClasses);
            }
            if (pluginOnlyResources != null) {
                ((SpringBootPluginClassLoader) pluginClassLoader).setPluginOnlyResources(pluginOnlyResources);
            }
        }

        BeanFactory beanFactory = new PluginListableBeanFactory(pluginClassLoader);
        Field beanFactoryField = ReflectionUtils.findField(
                applicationContext.getClass(), "beanFactory");
        if (beanFactoryField != null) {
            beanFactoryField.setAccessible(true);
            ReflectionUtils.setField(beanFactoryField, applicationContext, beanFactory);
        }
    }

    protected boolean importBean(ApplicationContext sourceApplicationContext,
                                 AbstractApplicationContext applicationContext,
                                 String beanName) {
        try {
            Object bean = sourceApplicationContext.getBean(beanName);
            applicationContext.getBeanFactory().registerSingleton(beanName, bean);
            applicationContext.getBeanFactory().autowireBean(bean);
            importedBeanNames.add(beanName);
            log.info("Bean {} is imported from {} ApplicationContext", beanName,
                    (sourceApplicationContext == mainApplicationContext ? "app" : "plugin"));
            return true;
        
        } catch (NoSuchBeanDefinitionException ex) {
            return false;
        }
    }

    protected boolean importBean(ApplicationContext sourceApplicationContext,
                                 AbstractApplicationContext applicationContext,
                                 Class<?> beanClass) {
        try {
            Map<String, ?> beans = sourceApplicationContext.getBeansOfType(beanClass);
            if (beans.size() <= 0) {
                return false;
            }
            for (String beanName : beans.keySet()) {
                if (applicationContext.containsBean(beanName)) continue;
                Object bean = beans.get(beanName);
                applicationContext.getBeanFactory().registerSingleton(beanName, bean);
                importedBeanNames.add(beanName);
                applicationContext.getBeanFactory().autowireBean(bean);
            }
            log.info("Bean {} is registered from {} ApplicationContext", beanClass.getSimpleName(),
                        (sourceApplicationContext == mainApplicationContext ? "app" : "plugin"));
            return true;
        } catch (NoSuchBeanDefinitionException ex) {
            return false;
        }
    }

    protected boolean importBeanFromMainContext(AbstractApplicationContext applicationContext,
                                                String beanName) {
        return importBean(mainApplicationContext, applicationContext, beanName);
    }

    protected boolean importBeanFromMainContext(AbstractApplicationContext applicationContext,
                                                Class<?> beanClass) {
        return importBean(mainApplicationContext, applicationContext, beanClass);
    }

    protected boolean importBeanFromDependentPlugin(AbstractApplicationContext applicationContext,
                                                    String beanName) {
        for (PluginDependency dependency : plugin.getWrapper().getDescriptor().getDependencies()) {
            PluginWrapper dependentPlugin = plugin.getPluginManager().getPlugin(dependency.getPluginId());
            if (dependentPlugin == null) continue;
            SpringBootPlugin sbPlugin = (SpringBootPlugin) dependentPlugin.getPlugin();
            if (importBean(sbPlugin.getApplicationContext(), applicationContext, beanName)) return true;
        }
        return false;
    }

    protected boolean importBeanFromDependentPlugin(AbstractApplicationContext applicationContext,
                                                    Class<?> beanClass) {
        for (PluginDependency dependency : plugin.getWrapper().getDescriptor().getDependencies()) {
            PluginWrapper dependentPlugin = plugin.getPluginManager().getPlugin(dependency.getPluginId());
            if (dependentPlugin == null) continue;
            SpringBootPlugin sbPlugin = (SpringBootPlugin) dependentPlugin.getPlugin();
            if (importBean(sbPlugin.getApplicationContext(), applicationContext, beanClass)) return true;
        }
        return false;
    }

    private String getProperties(Environment env, String propName, int index) {
        String prop = env.getProperty(String.format("pf4j-plugin.%s[%s]", propName, index));
        if (prop == null) prop = env.getProperty(String.format("pf4j-plugin.%s.%s", propName, index));
        if (prop == null) prop = env.getProperty(String.format("pf4j-plugin.%s[%s]",
                String.join("-", StringUtils.splitByCharacterTypeCamelCase(propName)).toLowerCase(), index));
        if (prop == null) prop = env.getProperty(String.format("pf4j-plugin.%s.%s",
                String.join("-", StringUtils.splitByCharacterTypeCamelCase(propName)).toLowerCase(), index));
        return prop;
    }
    
    private <T> T getProperties(Environment env, String propName, Class<T> targetType, T defaultValue) {
        T prop = env.getProperty(String.format("pf4j-plugin.%s", propName), targetType);
        if (prop == null) prop = env.getProperty(String.format("pf4j-plugin.%s",
                String.join("-", StringUtils.splitByCharacterTypeCamelCase(propName)).toLowerCase()), 
                targetType, defaultValue);
        return prop;
    }

    public class ExcludeConfigurations extends MapPropertySource {
        ExcludeConfigurations() {
            super("Exclude Configurations", presetProperties);
        }
    }
}
