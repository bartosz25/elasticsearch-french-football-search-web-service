package com.waitingforcode.elasticsearch.aspect;


import com.waitingforcode.elasticsearch.config.ElasticSearchConfig;
import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SearchQueryAspect {

    private static final Logger LOGGER = Logger.getLogger(SearchQueryAspect.class);

    @Autowired
    private Client elasticSearchClient;

    @Pointcut("execution(* com.waitingforcode.elasticsearch.service.*.find*(..))")
    public void handleElasticSearchQuerying() {
        // only a handler definition
    }

    @Before("handleElasticSearchQuerying()")
    public void checkClusterHealth() {
        ClusterHealthRequestBuilder healthRequest = elasticSearchClient.admin().cluster().prepareHealth();
        healthRequest.setTimeout(TimeValue.timeValueSeconds(5));
        healthRequest.setIndices(ElasticSearchConfig.ALIAS);
        ClusterHealthResponse healthResponse = healthRequest.execute().actionGet();
        LOGGER.info("Cluster health: "+healthResponse);
        if (!isClusterReady(healthResponse.getStatus())) {
            LOGGER.error("Cluster is not in ready to handle query");
            throw new IllegalStateException("Search can't be made when cluster is not ready");
        }
    }

    private boolean isClusterReady(ClusterHealthStatus status) {
        return status != ClusterHealthStatus.RED;
    }

}
