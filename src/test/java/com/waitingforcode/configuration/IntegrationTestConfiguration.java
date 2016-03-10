package com.waitingforcode.configuration;


import org.elasticsearch.client.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(value = "com.waitingforcode")
@Profile("test")
public class IntegrationTestConfiguration {

    @Bean
    public EsEmbeddedServer esEmbeddedServer() {
        return new EsEmbeddedServer();
    }

    @Bean
    public Client elasticSearchClient(EsEmbeddedServer esEmbeddedServer) {
        return esEmbeddedServer.getClient();
    }

}
