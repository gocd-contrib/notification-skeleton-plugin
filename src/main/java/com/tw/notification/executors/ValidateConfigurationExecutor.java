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

package com.tw.notification.executors;

import com.tw.notification.RequestExecutor;
import com.tw.notification.requests.ValidatePluginSettings;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.Map;

public class ValidateConfigurationExecutor implements RequestExecutor {
    private static final Gson GSON = new Gson();

    private final ValidatePluginSettings settings;

    public ValidateConfigurationExecutor(ValidatePluginSettings settings) {
        this.settings = settings;
    }

    public GoPluginApiResponse execute() {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        for (Map.Entry<String, Field> entry : GetPluginConfigurationExecutor.FIELDS.entrySet()) {
            Field field = entry.getValue();
            Map<String, String> validationError = field.validate(settings.get(entry.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }
}
