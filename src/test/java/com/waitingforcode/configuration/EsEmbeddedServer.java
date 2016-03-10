package com.waitingforcode.configuration;

import com.waitingforcode.elasticsearch.config.ElasticSearchConfig;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.Version;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.groovy.GroovyPlugin;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

public class EsEmbeddedServer implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = Logger.getLogger(EsEmbeddedServer.class);

    private static final Path MAPPING_INDEX = Paths.get(EsEmbeddedServer.class.getClassLoader().getResource("mapping/mapping.json").getPath());

    private Client client;

    private Node node;

    public EsEmbeddedServer() {
        //Settings settings = InternalSettingsPreparer.prepareSettings(ImmutableSettings.Builder.EMPTY_SETTINGS, true).v1();
        //LogConfigurator.configure(ImmutableSettings.settingsBuilder().put("path.conf", RESOURCES + "/logs").build());
        Settings.Builder elasticsearchSettings = Settings.settingsBuilder()
                .put("path.home", "target")
                .put("http.enabled", false)
                .put("path.data", System.getProperty("env.testBase", "target") + "/test-es-data")
                .put("script.engine.groovy.inline.aggs", "true")
                .put("script.engine.groovy.inline.search", "true")
                .put("index.max_result_window", 2147483647)
                .put("node.name", "integration_test")
                // because we use customized Node object, put properties inline
                .put("cluster.name", "test")
                .put("node.data", true)
                .put("node.local", true)
                .put("client.type", "node")
                ;
        node = new ConfigurableNode(elasticsearchSettings.build(), Collections.<Class<? extends Plugin>>singleton(GroovyPlugin.class));

        /**
         * Manual start is needed, otherwise request doesn't arrive at time. One PR should resolve the issue:
         * <a href="https://github.com/elastic/elasticsearch/pull/16746">Pull Request to solve embedded ES health issue</a>
         */
        node.start();

        // wait for yellow/green status before continue
        node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        client = node.client();
        // Create index if not exists
        boolean exists = client.admin().indices().prepareExists(ElasticSearchConfig.ALIAS).execute().actionGet().isExists();
        if (!exists) {
            CreateIndexRequest request = Requests.createIndexRequest(ElasticSearchConfig.ALIAS)
                    .source(Files.readAllBytes(MAPPING_INDEX));
            CreateIndexResponse response = client.admin().indices().create(request).actionGet();
            if (!response.isAcknowledged()) {
                throw new IllegalStateException("Index was not correctly created");
            }
        } else {
            LOGGER.debug("Index "+ElasticSearchConfig.ALIAS+" already exists, do not recreate it");
        }
    }

    @Override
    public void destroy() throws IOException {
        client.close();
        node.close();
        FileUtils.deleteDirectory(Paths.get(System.getProperty("env.testBase", "target")+"/test-es-data").toFile());
    }

    public Client getClient() {
        return client;
    }

    // comes from http://stackoverflow.com/questions/35582520/elasticsearch-script-lang-not-supported-groovy
    // to solve issue about missing Groovy plugin
    private static class ConfigurableNode extends Node {
        public ConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, null),
                    Version.CURRENT,
                    classpathPlugins);
        }
    }


}
