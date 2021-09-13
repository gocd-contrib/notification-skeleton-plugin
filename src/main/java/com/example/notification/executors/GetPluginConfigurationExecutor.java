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

package com.example.notification.executors;

import com.example.notification.RequestExecutor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.nio.file.WatchEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * TODO: add any additional configuration fields here.
 */
public class GetPluginConfigurationExecutor implements RequestExecutor {

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final Field GO_SERVER_URL = new NonBlankField("go_server_url", "Go Server URL", null, true, false, "0");
    public static final Field API_SERVER_URL = new NonBlankField("api_url", "API URL", null, true, false, "1");
    public static final Field API_USER = new NonBlankField("api_user", "API User", null, true, false, "2");
    public static final Field API_KEY = new NonBlankField("api_key", "API Key", null, true, false, "3");
    public static final Field CHAT_WEBHOOK_URL = new NonBlankField("web_hook_url", "Web hook URL", null, true, false, "4");

    public static final Map<String, Field> FIELDS = new LinkedHashMap<>();

    static {
        FIELDS.put(GO_SERVER_URL.key(), GO_SERVER_URL);

        FIELDS.put(API_SERVER_URL.key(), API_SERVER_URL);
        FIELDS.put(API_USER.key(), API_USER);
        FIELDS.put(API_KEY.key(), API_KEY);
        FIELDS.put(CHAT_WEBHOOK_URL.key(), CHAT_WEBHOOK_URL);
    }

    public GoPluginApiResponse execute() {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(FIELDS));
    }
}
