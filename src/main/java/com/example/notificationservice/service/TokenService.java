package com.example.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final RestClient restClient = RestClient.create();
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    private String accessToken;
    private Instant expiresAt = Instant.EPOCH;

    public TokenService(@Value("${notification.auth.token-url}") String tokenUrl,
                        @Value("${notification.auth.client-id}") String clientId,
                        @Value("${notification.auth.client-secret}") String clientSecret,
                        @Value("${notification.auth.scope}") String scope) {
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    public synchronized String getToken() {
        if (accessToken == null || Instant.now().isAfter(expiresAt.minusSeconds(30))) {
            fetchNewToken();
        }
        return accessToken;
    }

    private void fetchNewToken() {
        log.info("Fetching a new access token from {}", tokenUrl);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("scope", scope);

        Map<String, Object> body = restClient.post()
                .uri(tokenUrl)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (body == null || body.get("access_token") == null) {
            throw new IllegalStateException("Token response had no access_token");
        }
        accessToken = (String) body.get("access_token");
        long expiresIn = ((Number) body.get("expires_in")).longValue();
        expiresAt = Instant.now().plusSeconds(expiresIn);
    }
}