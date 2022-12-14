package org.rsinitsyn.bot.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration")
@Data
public class ClientProperties {

    private AbstractClient google;

    @Data
    public static class AbstractClient {
        private String redirectUri;
        private String clientId;
        private String clientSecret;
    }
}
