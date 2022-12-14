package org.rsinitsyn.bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleApiService {

    private final WebClient webClient;

    public JsonNode getUserDetails(String accessToken) {
        // https://www.googleapis.com/oauth2/v3/userinfo
        // "https://www.googleapis.com/drive/v3/files"
        return webClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));
    }
}
