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

import java.net.URI;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// Implement any settings that your plugin needs
public class PluginSettings {
    private static final Gson GSON = new GsonBuilder().
            excludeFieldsWithoutExposeAnnotation().
            create();

    @Expose
    @SerializedName("webhook_uris")
    private String webhookURIsValue;

    public static PluginSettings fromJSON(String json) {
        return GSON.fromJson(json, PluginSettings.class);
    }

    public URI[] getWebhookURIs() {
        ArrayList<URI> uris = new ArrayList<>();
        String[] lines = webhookURIsValue.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.length() > 0) {
                try {
                    URI parsedUri = new URI(line);
                    // Only allow HTTPS URIs
                    if (parsedUri.getScheme() == null || !parsedUri.getScheme().equals("https")) {
                        System.out.println("URI must use HTTPS: " + line);
                        continue;
                    }
                    uris.add(new URI(line));
                } catch (Exception e) {
                    System.out.println("Invalid URI: " + line);
                }
            }
        }
        return uris.toArray(new URI[uris.size()]);
    }
}
