package com.waitingforcode.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitingforcode.elasticsearch.config.ElasticSearchConfig;
import com.waitingforcode.elasticsearch.data.IndexTypes;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteAction;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Install datasets tested in integration tests.
 */
@Component
public class Indexers {

    private static final String BASE_DIR = Indexers.class.getResource("/data").getPath();
    private static final String EXTENSION = ".json";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private Client elasticsearchClient;

    public void teamsFromFile(Collection<String> files) {
        files.forEach((String file) -> {
            try {
                String content = new String(Files.readAllBytes(Paths.get(BASE_DIR + "/teams/" + file + EXTENSION)));
                IndexResponse response = elasticsearchClient.prepareIndex(ElasticSearchConfig.ALIAS, IndexTypes.TEAMS.getType())
                        .setSource(content)
                        .execute()
                        .actionGet();
                if (!response.isCreated()) {
                    throw new IndexingException("Document was not correctly indexed");
                }
            } catch (IOException e) {
                throw new IndexingException(e);
            }
        });
        refreshIndex();
    }

    public void tablesFromObjects(Collection<TableInput> tables) {
        tables.forEach((TableInput table) -> {
            IndexResponse response = null;
            try {
                response = elasticsearchClient.prepareIndex(ElasticSearchConfig.ALIAS, IndexTypes.TABLES.getType())
                        .setSource(MAPPER.writeValueAsString(table))
                        .execute()
                        .actionGet();
                if (!response.isCreated()) {
                    throw new IndexingException("Document was not correctly indexed");
                }
            } catch (JsonProcessingException e) {
                throw new IndexingException(e);
            }
        });
        refreshIndex();
    }

    public void scoresFromObject(Collection<ScoreInput> scores) {
        scores.forEach((ScoreInput score) -> {
            IndexResponse response = null;
            try {
                response = elasticsearchClient.prepareIndex(ElasticSearchConfig.ALIAS, IndexTypes.SCORES.getType())
                        .setSource(MAPPER.writeValueAsString(score))
                        .execute()
                        .actionGet();
                if (!response.isCreated()) {
                    throw new IndexingException("Document was not correctly indexed");
                }
            } catch (JsonProcessingException e) {
                throw new IndexingException(e);
            }
        });
        refreshIndex();
    }

    public void cleanAll(String...types) {
        SearchResponse response = elasticsearchClient.prepareSearch(ElasticSearchConfig.ALIAS)
                .setTypes(types)
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(Integer.MAX_VALUE)
                .execute()
                .actionGet();
        BulkRequestBuilder bulkRequest = elasticsearchClient.prepareBulk();
        for (SearchHit hit : response.getHits().getHits()) {
            bulkRequest.add(new DeleteRequestBuilder(elasticsearchClient, DeleteAction.INSTANCE, ElasticSearchConfig.ALIAS)
                    .setId(hit.getId()).setType(hit.getType()));
        }
        BulkResponse bulkResponse = bulkRequest.setRefresh(true).get();
        if (bulkResponse.hasFailures()) {
            throw new IndexingException("Indices cleaning failed");
        }
    }

    private void refreshIndex() {
        RefreshResponse response = elasticsearchClient.admin().indices().prepareRefresh(ElasticSearchConfig.ALIAS).get();
        if (response.getFailedShards() > 0) {
            throw new IndexingException("An error occurred on refreshing indexes");
        }
    }

    private static final class IndexingException extends RuntimeException {
        public IndexingException(String message) {
            super(message);
        }

        public IndexingException(Throwable cause) {
            super(cause);
        }
    }
}
