package net.getsentry.gocd.webhooknotifier.utils;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import net.getsentry.gocd.webhooknotifier.PluginRequest;
import net.getsentry.gocd.webhooknotifier.PluginSettings;
import net.getsentry.gocd.webhooknotifier.ServerRequestFailedException;
import net.getsentry.gocd.webhooknotifier.URLWithAuth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.FieldNamingPolicy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import io.sentry.Sentry;
import io.sentry.ISpan;

public class Http {
  private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  protected static final String SIGNATURE_HEADER = "x-gocd-signature";
  protected static final String GCP_AUTH_METADATA_URL = "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity?audience=";

  private static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(Date.class, new DefaultDateTypeAdapter(DATE_PATTERN))
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create();

  public static void pingWebhooks(PluginRequest pluginRequest, String type, Object originalPayload, HttpClient client)
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
    
    for (URLWithAuth urlWithAuth : urlWithAuths) {
      try {
        List<Header> headers = new ArrayList<Header>();
        URL url = urlWithAuth.getUrl();
        if (url == null) {
          continue;
        }
        String authToken = Auth.getAuthToken(urlWithAuth, client);
        if (authToken != null) {
          headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken));
        }
        if (urlWithAuth.getSecretValue() != null) {
          headers.add(new BasicHeader("x-gocd-signature", Auth.createSignature(responseJsonStr, urlWithAuth.getSecretValue())));
        }

        ISpan httpPostSpan = webhooksSpan.startChild("http.post");
        httpPostSpan.setData("webhook.url", url.toString());
        HttpResponse response = post(url, responseJsonStr, client, headers.toArray(new Header[0]));
        int statusCode = response.getStatusLine().getStatusCode();
        httpPostSpan.setData("webhook.status_code", statusCode);
        httpPostSpan.finish();
      } catch (Exception e) {
        System.out.printf("    😺 failed to post request to %s with audience %s: %s\n", urlWithAuth.getUrl(), urlWithAuth.getAudience(), e.getMessage());
      }
    }
    webhooksSpan.finish();
  }

  public static void pingWebhooks(PluginRequest pluginRequest, String type, Object originalPayload)
      throws ServerRequestFailedException {
    HttpClient httpClient = HttpClientBuilder.create().build();
    pingWebhooks(pluginRequest, type, originalPayload, httpClient);
  }

  protected static HttpResponse post(URL endpoint, String requestBody, HttpClient client, Header... headers)
      throws UnsupportedEncodingException, IOException {
    HttpPost post = new HttpPost(endpoint.toString());
    post.setEntity(new StringEntity(requestBody));
    post.setHeader("Content-type", "application/json");
    for (Header header : headers) {
      post.setHeader(header);
    }
    return client.execute(post);
  }

  protected static HttpResponse post(URL endpoint, String requestBody, Header... headers)
      throws UnsupportedEncodingException, IOException {
    HttpClient httpClient = HttpClientBuilder.create().build();
    return post(endpoint, requestBody, httpClient, headers);
  }
}
