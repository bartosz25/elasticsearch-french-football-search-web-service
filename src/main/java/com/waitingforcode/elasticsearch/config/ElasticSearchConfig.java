package com.waitingforcode.elasticsearch.config;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("app")
public class ElasticSearchConfig {

    public static final String CLUSTER_NAME = "waitingforcode";
    public static final String ALIAS = "french_football";

    /**
     * Constant to apply only when we are not expecting thousands of results.
     */
    public static final int DEFAULT_MAX_RESULTS = 999;

    @Bean
    public Client elasticSearchClient() {
        return NodeBuilder.nodeBuilder().client(true).clusterName(CLUSTER_NAME)
                .node().client();
    }

}
