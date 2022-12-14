package org.rsinitsyn.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Oauth2TokenResponse(@JsonProperty("access_token") String accessToken,
                                  @JsonProperty("refresh_token") String refreshToken,
                                  @JsonProperty("scope") String scope,
                                  @JsonProperty("expires_in") String expiresIn,
                                  @JsonProperty("token_type") String tokenType) {
}
