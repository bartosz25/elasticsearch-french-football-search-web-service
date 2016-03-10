package com.waitingforcode.elasticsearch.aspect;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SearchQueryAspectTest {

    @InjectMocks
    private SearchQueryAspect searchQueryAspect = new SearchQueryAspect();

    @Mock
    private Client elasticsearchClient;

    @Mock
    private AdminClient adminClient;

    @Mock
    private ClusterAdminClient clusterAdminClient;

    @Mock
    private ClusterHealthRequestBuilder clusterHealthRequestBuilder;

    @Mock
    private ListenableActionFuture<ClusterHealthResponse> responseListenableActionFuture;

    @Mock
    private ClusterHealthResponse clusterHealthResponse;

    @Before
    public void initMocks() {
        when(elasticsearchClient.admin()).thenReturn(adminClient);
        when(adminClient.cluster()).thenReturn(clusterAdminClient);
        when(clusterAdminClient.prepareHealth()).thenReturn(clusterHealthRequestBuilder);
        when(clusterHealthRequestBuilder.execute()).thenReturn(responseListenableActionFuture);
        when(responseListenableActionFuture.actionGet()).thenReturn(clusterHealthResponse);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_an_exception_on_not_ready_cluster() {
        when(clusterHealthResponse.getStatus()).thenReturn(ClusterHealthStatus.RED);

        searchQueryAspect.checkClusterHealth();
    }

    @Test
    public void should_pass_for_ready_cluster() {
        when(clusterHealthResponse.getStatus()).thenReturn(ClusterHealthStatus.YELLOW);

        searchQueryAspect.checkClusterHealth();

        verify(clusterHealthResponse).getStatus();
    }

}
