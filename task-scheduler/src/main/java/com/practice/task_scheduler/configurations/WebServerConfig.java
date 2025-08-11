package com.practice.task_scheduler.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerConfig {

    @Value("${server.tomcat.threads.max}")
    private String maxThreads;

    @Value("${server.tomcat.threads.min-spare}")
    private String minSpareThreads;

    @Value("${server.tomcat.max-connections}")
    private String maxConnections;

    @Value("${server.tomcat.accept-count}")
    private String acceptCount;

    @Value("${server.tomcat.timeout-connection}")
    private String connectionTimeout;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setProperty("maxThreads", maxThreads);
                connector.setProperty("minSpareThreads", minSpareThreads);
                connector.setProperty("maxConnections", maxConnections);
                connector.setProperty("acceptCount", acceptCount);
                connector.setProperty("connectionTimeout", connectionTimeout);
            });
        };
    }
}
