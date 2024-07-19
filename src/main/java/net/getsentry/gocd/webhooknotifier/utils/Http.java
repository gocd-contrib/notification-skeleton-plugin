package net.getsentry.gocd.webhooknotifier.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import net.getsentry.gocd.webhooknotifier.PluginRequest;
import net.getsentry.gocd.webhooknotifier.PluginSettings;
import net.getsentry.gocd.webhooknotifier.ServerRequestFailedException;
import net.getsentry.gocd.webhooknotifier.URLAudiencePair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.FieldNamingPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class Http {
  protected static final String GCP_AUTH_METADATA_URL = "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity?audience=";

  private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
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

    URLAudiencePair[] urlAudiencePairs = ps.getWebhooks();
    for (URLAudiencePair urlAudiencePair : urlAudiencePairs) {
      try {
        List<Header> headers = new ArrayList<Header>();
        URL url = urlAudiencePair.getUrl();
        if (url == null) {
          continue;
        }
        String authToken = getAuthToken(urlAudiencePair, client);
        if (authToken != null) {
          headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken));
        }
        post(url, responseJsonStr, client, headers.toArray(new Header[0]));
      } catch (Exception e) {
        System.out.printf("    😺 failed to post request to %s with audience %s: %s\n", urlAudiencePair.getUrl(), urlAudiencePair.getAudience(), e.getMessage());
      }
    }
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

  protected static String getAuthToken(URLAudiencePair urlAudiencePair, HttpClient client) {
    String audience = urlAudiencePair.getAudience();
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

  protected static String getAuthToken(URLAudiencePair urlAudiencePair) {
    HttpClient httpClient = HttpClientBuilder.create().build();
    return getAuthToken(urlAudiencePair, httpClient);
  }
}
