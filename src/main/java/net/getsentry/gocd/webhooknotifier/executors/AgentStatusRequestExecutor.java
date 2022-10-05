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

import com.example.notification.PluginRequest;
import com.example.notification.RequestExecutor;
import com.example.notification.requests.AgentStatusRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Arrays;
import java.util.HashMap;

public class AgentStatusRequestExecutor implements RequestExecutor {
    private final AgentStatusRequest request;
    private final PluginRequest pluginRequest;

    public AgentStatusRequestExecutor(AgentStatusRequest request, PluginRequest pluginRequest) {
        this.request = request;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        HashMap<String, Object> responseJson = new HashMap<>();
        try {
            sendNotification();
            responseJson.put("status", "success");
        } catch (Exception e) {
            responseJson.put("status", "failure");
            responseJson.put("messages", Arrays.asList(e.getMessage()));
        }
        return new DefaultGoPluginApiResponse(200, AgentStatusRequest.GSON.toJson(responseJson));
    }

    protected void sendNotification() throws Exception {
        // TODO: Implement this. The request.agent object has all the details about the state changed agent
        // If you need access to settings like API keys, URLs, then call PluginRequest#getPluginSettings
//        PluginSettings pluginSettings = pluginRequest.getPluginSettings();
        throw new UnsupportedOperationException();
    }
}
