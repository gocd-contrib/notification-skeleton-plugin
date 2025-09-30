package net.getsentry.gocd.webhooknotifier.utils;

import net.getsentry.gocd.webhooknotifier.PluginRequest;
import net.getsentry.gocd.webhooknotifier.PluginSettings;
import net.getsentry.gocd.webhooknotifier.ServerRequestFailedException;
import net.getsentry.gocd.webhooknotifier.URLWithAuth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.FieldNamingPolicy;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.sentry.Sentry;
import io.sentry.ISpan;

public class Http {
  private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  protected static final String SIGNATURE_HEADER = "x-gocd-signature";
  protected static final String GCP_AUTH_METADATA_URL = "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity?audience=";

  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .build();

  private static final ExecutorService WEBHOOK_EXECUTOR = Executors.newFixedThreadPool(10);

  private static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(Date.class, new DefaultDateTypeAdapter(DATE_PATTERN))
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create();

  public static void pingWebhooks(PluginRequest pluginRequest, String type, Object originalPayload)
      throws ServerRequestFailedException {
    HashMap<String, Object> responseJson = new HashMap<>();
    responseJson.put("type", type);
    responseJson.put("data", originalPayload);

    String responseJsonStr = GSON.toJson(responseJson);

    PluginSettings ps = pluginRequest.getPluginSettings();
    if (ps == null) {
      // This can occur when the plugin is first installed
      return;
    }

    URLWithAuth[] urlWithAuths = ps.getWebhooks();
    ISpan webhooksSpan = Sentry.startTransaction("webhook.notification", type);
    
    WEBHOOK_EXECUTOR.submit(() -> {
      try {
        for (URLWithAuth urlWithAuth : urlWithAuths) {
          sendWebhook(urlWithAuth, responseJsonStr, webhooksSpan);
        }
      } finally {
        webhooksSpan.finish();
      }
    });
  }

  private static void sendWebhook(URLWithAuth urlWithAuth, String responseJsonStr, ISpan parentSpan) {
    URL url = urlWithAuth.getUrl();
    if (url == null) {
      return;
    }

    ISpan httpPostSpan = parentSpan.startChild("http.post");
    httpPostSpan.setData("webhook.url", url.toString());
    
    try {
      HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
          .uri(url.toURI())
          .timeout(Duration.ofSeconds(10))
          .header("Content-Type", "application/json");

      String authToken = Auth.getAuthToken(urlWithAuth);
      if (authToken != null) {
        requestBuilder.header("Authorization", "Bearer " + authToken);
      }
      if (urlWithAuth.getSecretValue() != null) {
        requestBuilder.header("x-gocd-signature", Auth.createSignature(responseJsonStr, urlWithAuth.getSecretValue()));
      }

      HttpRequest request = requestBuilder
          .POST(HttpRequest.BodyPublishers.ofString(responseJsonStr))
          .build();

      System.out.printf("Sending webhook to %s\n", url);

      CompletableFuture<HttpResponse<String>> future = HTTP_CLIENT.sendAsync(
          request, 
          HttpResponse.BodyHandlers.ofString()
      );

      HttpResponse<String> response = future.get();
      int statusCode = response.statusCode();
      httpPostSpan.setData("webhook.status_code", statusCode);
      httpPostSpan.finish();
    } catch (Exception e) {
      System.out.printf("    😺 failed to post request to %s with audience %s: %s\n", url, e.getMessage());
      Sentry.captureException(e);
      httpPostSpan.finish();
    }
  }

  // Legacy method for testing
  protected static HttpResponse<String> post(URL endpoint, String requestBody, String... headers) throws Exception {
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
        .uri(endpoint.toURI())
        .timeout(Duration.ofSeconds(10))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody));

    for (int i = 0; i < headers.length; i += 2) {
      if (i + 1 < headers.length) {
        requestBuilder.header(headers[i], headers[i + 1]);
      }
    }

    HttpRequest request = requestBuilder.build();
    return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
  }
}
