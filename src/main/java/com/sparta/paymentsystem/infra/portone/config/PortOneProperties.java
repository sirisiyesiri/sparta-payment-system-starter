package com.sparta.paymentsystem.infra.portone.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "portone")
@Getter @Setter
public class PortOneProperties {
    private String baseUrl;
    private String apiSecret;
    private String storeId;
    private String channelKey;
}