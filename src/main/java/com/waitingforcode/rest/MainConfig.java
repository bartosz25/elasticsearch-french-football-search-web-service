package com.waitingforcode.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/rest")
@Component
public class MainConfig extends ResourceConfig {

    public MainConfig() {
        // Scans packages in com.waitingforcode.rest during runtime
        packages("com.waitingforcode.rest");
    }



}
