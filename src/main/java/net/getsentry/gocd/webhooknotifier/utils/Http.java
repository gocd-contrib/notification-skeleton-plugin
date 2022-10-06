package net.getsentry.gocd.webhooknotifier.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import net.getsentry.gocd.webhooknotifier.PluginRequest;
import net.getsentry.gocd.webhooknotifier.PluginSettings;
import net.getsentry.gocd.webhooknotifier.ServerRequestFailedException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;

import java.util.HashMap;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Http {
  private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

  public static void PingWebhooks(PluginRequest pluginRequest, String type, Object originalPayload) throws ServerRequestFailedException {
    HashMap<String, Object> responseJson = new HashMap<>();
    responseJson.put("type", type);
    responseJson.put("data", originalPayload);

    String responseJsonStr = GSON.toJson(responseJson);

    PluginSettings ps = pluginRequest.getPluginSettings();
    String[] urls = ps.getTrimmedWebhookURLs();
    for (int i = 0; i < urls.length; i++) {
        System.out.printf("😺 StageStatusRequestExecutor->sendNotification() send update to: %s\n", urls[i]);
        try {
          Post(urls[i], responseJsonStr);
        } catch (Exception e) {
          System.out.printf("    😺 failed to post request to %s: %s\n", urls[i], e.getMessage());
        }
    }
  }

  private static HttpResponse Post(String endpoint, String requestBody) throws UnsupportedEncodingException, IOException {
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(endpoint);
    post.setEntity(new StringEntity(requestBody));
    post.setHeader("Content-type", "application/json");
    return httpClient.execute(post);
  }
}
