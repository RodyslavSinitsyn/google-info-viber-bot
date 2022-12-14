package org.rsinitsyn.bot.api;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rsinitsyn.bot.model.Oauth2TokenResponse;
import org.rsinitsyn.bot.props.ClientProperties;
import org.rsinitsyn.bot.utils.TokenUtils;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class AuthApi {

    private final ClientProperties properties;
    private final WebClient webClient;

    @RequestMapping("/google")
    public String getLoginInfoAsGoogle(@RequestParam("code") String code, @RequestParam("scope") String scopes) {
        log.info("Callback from google, code: {}, scope: {}", code, scopes);

        Oauth2TokenResponse tokenResponse = getAccessToken(code, scopes);
        log.info("Response from /oauth2/token: {}", tokenResponse);

        return tokenResponse.accessToken();
    }

    public Oauth2TokenResponse getAccessToken(String code, String scope) {
        String clientCredentials = TokenUtils.getBasicToken(
                properties.getGoogle().getClientId(),
                properties.getGoogle().getClientSecret());

        MultiValueMap<String, String> formDataBody = new LinkedMultiValueMap<>();
        formDataBody.add("code", code);
        formDataBody.add("grant_type", "authorization_code");
        formDataBody.add("redirect_uri", properties.getGoogle().getRedirectUri());
        formDataBody.add("scope", scope);

        // https://www.googleapis.com/oauth2/v4/token
        // https://accounts.google.com/o/oauth2/token
        return webClient.post()
                .uri("https://www.googleapis.com/oauth2/v4/token")
                .headers(httpHeaders -> {
                    httpHeaders.setBasicAuth(clientCredentials);
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .body(BodyInserters.fromFormData(formDataBody))
                .retrieve()
                .bodyToMono(Oauth2TokenResponse.class)
                .block(Duration.ofSeconds(30));
    }
}
