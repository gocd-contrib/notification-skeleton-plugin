package net.getsentry.gocd.webhooknotifier.utils;

import net.getsentry.gocd.webhooknotifier.URLWithAuth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class Auth {
    protected static final String SIGNATURE_HEADER = "x-gocd-signature";
    protected static final String GCP_AUTH_METADATA_URL = "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity?audience=";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    protected static String createSignature(String payload, String secret) throws NoSuchAlgorithmException {
        Mac sha256Mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), sha256Mac.getAlgorithm());
        try {
            sha256Mac.init(secretKeySpec);
        } catch (Exception e) {
            throw new NoSuchAlgorithmException("Failed to initialize HmacSHA256: " + e.getMessage());
        }
        byte[] macBytes = sha256Mac.doFinal(payload.getBytes());
        StringBuilder result = new StringBuilder();
        for (byte b : macBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    protected static String getAuthToken(URLWithAuth urlWithAuth) {
        String audience = urlWithAuth.getAudience();
        if (audience == null) {
            return null;
        }
        String authUrl = GCP_AUTH_METADATA_URL + audience;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                .timeout(Duration.ofSeconds(5))
                .header("Metadata-Flavor", "Google")
                .GET()
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            System.out.printf("failed to get auth token from %s: %s\n", authUrl, e.getMessage());
            return null;
        }
    }
}
