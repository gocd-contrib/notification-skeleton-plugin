package net.getsentry.gocd.webhooknotifier.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import net.getsentry.gocd.webhooknotifier.URLWithAuth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.security.NoSuchAlgorithmException;

public class Auth {
    protected static final String SIGNATURE_HEADER = "x-gocd-signature";
    protected static final String GCP_AUTH_METADATA_URL = "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity?audience=";

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

    protected static String getAuthToken(URLWithAuth urlWithAuth, HttpClient client) {
        String audience = urlWithAuth.getAudience();
        if (audience == null) {
            return null;
        }
        String authUrl = GCP_AUTH_METADATA_URL + audience;
        try {
            HttpGet get = new HttpGet(authUrl);
            get.setHeader("Metadata-Flavor", "Google");
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            System.out.printf("failed to get auth token from %s: %s\n", authUrl, e.getMessage());
            return null;
        }
    }

    protected static String getAuthToken(URLWithAuth urlWithAuth) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        return getAuthToken(urlWithAuth, httpClient);
    }
}
