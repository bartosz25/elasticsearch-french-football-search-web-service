package com.waitingforcode.client;


import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for clients available in Elasticsearch Java API. Currently only transport client is tested. It's an
 * integration test, against already running server with default configuration parameters.
 *
 * It's ignored because it doesn't test application features but Elasticsearch ones. The test is here because of the article about
 * <a href="http://www.waitingforcode.com/elasticsearch/connection-modes-in-elasticsearch/read" target="_blank">connection modes in Elasticsearch</a>
 *
 * Before executing, launch Elasticsearch on port 9300, create index with type and add some items:
 * 1) Create index & type
 * PUT http://localhost:9200/teams
 * <code>
 *     {
 *       "mappings": {
 *         "team": {"properties" : {"name" : { "type" : "string"}}}
 *         }
 *     }
 * </code>
 * 2) Add items
 * POST http://localhost:9200/teams/_bulk
 * <code>
 * {"index": {"_index": "teams", "_type": "team"}}
 * {"name": "RC Paris"}
 * {"index": {"_index": "teams", "_type": "team"}}
 * {"name": "Roubaix"}
 * {"index": {"_index": "teams", "_type": "team"}}
 * {"name": "Nimes"}
 *
 * </code>
 *
 */
@Ignore
public class TransportClientIntegrationTest {

    private static final Settings SETTINGS = Settings.settingsBuilder()
            .put("cluster.name", "waitingforcode").build();

    @Test
    public void test_connection_through_transport_client() throws UnknownHostException {
        Client client = new TransportClient.Builder().settings(SETTINGS).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        // Sample request to count all indexed teams
        SearchResponse response = getTestQuery(client);

        assertThat(response.getHits().getTotalHits()).isGreaterThan(0L);
    }

    @Test(expected = NoNodeAvailableException.class)
    public void test_connection_to_bad_port() throws UnknownHostException {
        // When we try to connect to invalid cluster, the failure is silent.
        // Only the query executed with invalid client causes an exception.
        Client client = new TransportClient.Builder().settings(SETTINGS).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 1234));

        getTestQuery(client);
    }

    private SearchResponse getTestQuery(Client client) {
        SearchRequestBuilder countRequestBuilder = new SearchRequestBuilder(client, SearchAction.INSTANCE).setIndices("teams").setTypes("team")
                .setSize(0).setQuery(QueryBuilders.matchAllQuery());
        ActionFuture<SearchResponse> responseFuture = client.search(countRequestBuilder.request());
        return responseFuture.actionGet();
    }

}
