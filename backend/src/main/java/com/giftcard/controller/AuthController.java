package com.giftcard.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * The frontend never sees the Casdoor client secret. It sends us the
 * short-lived `code` it got back from Casdoor's login redirect, and we do
 * the code-for-token exchange server-side, then hand the resulting
 * access_token back to the frontend to use as a Bearer token on all
 * subsequent /api/** calls.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${casdoor.base-url}")
    private String casdoorBaseUrl;

    @Value("${casdoor.client-id}")
    private String clientId;

    @Value("${casdoor.client-secret}")
    private String clientSecret;

    @Value("${casdoor.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public static class TokenRequest {
        public String code;
    }

    @PostMapping("/token")
    public ResponseEntity<Map> exchangeToken(@RequestBody TokenRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", request.code);
        form.add("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(form, headers);

        // Casdoor's token endpoint - returns { access_token, token_type, expires_in,
        // refresh_token, ... }
        ResponseEntity<Map> response = restTemplate.postForEntity(
                casdoorBaseUrl + "/api/login/oauth/access_token", httpRequest, Map.class);

        return ResponseEntity.ok(response.getBody());
    }
}