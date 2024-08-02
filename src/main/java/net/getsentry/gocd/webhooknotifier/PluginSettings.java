/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.getsentry.gocd.webhooknotifier;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;

// Implement any settings that your plugin needs
public class PluginSettings {
    private static final ConcurrentHashMap<String, String> secretCache = new ConcurrentHashMap<>();
    private static final Gson GSON = new GsonBuilder().
            excludeFieldsWithoutExposeAnnotation().
            create();

    @Expose
    @SerializedName("webhooks")
    private String webhooksValue;

    public static PluginSettings fromJSON(String json) {
        return GSON.fromJson(json, PluginSettings.class);
    }

    public URLWithAuth[] getWebhooks(SecretManagerServiceClient client) {
        ArrayList<URLWithAuth> urlWithAuths = new ArrayList<>();
        String[] lines = webhooksValue.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.length() > 0) {
                try {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        if (isGCPSecret(parts[1].trim())) {
                            if (client == null) {
                                throw new Exception("SecretManagerServiceClient is null");
                            }
                            urlWithAuths.add(new URLWithAuth(parts[0].trim(), null, getSecretValue(client, parts[1].trim())));
                        } else {
                            urlWithAuths.add(new URLWithAuth(parts[0].trim(), parts[1].trim()));
                        }
                    } else {
                        urlWithAuths.add(new URLWithAuth(parts[0].trim(), null));
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing webhook: " + line);
                }
            }
        }
        return urlWithAuths.toArray(new URLWithAuth[urlWithAuths.size()]);
    }

    public URLWithAuth[] getWebhooks() {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            return getWebhooks(client);
        } catch (Exception e) {
            System.out.println("Error creating SecretManagerServiceClient");
            return getWebhooks(null);
        }
    }

    private boolean isGCPSecret(String value) {
        return value.startsWith("gcp-secret:");
    }

    private String getSecretValue(SecretManagerServiceClient client, String secretReference) {
        return secretCache.computeIfAbsent(secretReference, key -> {
            try {
                String secretName = secretReference.substring("gcp-secret:".length());
                String[] parts = secretName.split("/");
                String secretId = parts[parts.length - 1];
                String projectId = parts[1];
                AccessSecretVersionResponse response = client.accessSecretVersion("projects/" + projectId + "/secrets/" + secretId + "/versions/latest");
                return response.getPayload().getData().toStringUtf8();
            } catch (Exception e) {
                System.out.println("Error fetching secret: " + secretReference);
                return null;
            }
        });
    }
}
