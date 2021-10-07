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

package com.tw.notification.requests;

import com.tw.notification.PluginRequest;
import com.tw.notification.RequestExecutor;
import com.tw.notification.executors.AgentStatusRequestExecutor;
import com.tw.notification.utils.DefaultDateTypeAdapter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class AgentStatusRequest {
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new DefaultDateTypeAdapter(DATE_PATTERN))
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public Agent agent;

    public static AgentStatusRequest fromJSON(String json) {
        return GSON.fromJson(json, AgentStatusRequest.class);
    }

    public RequestExecutor executor(PluginRequest pluginRequest) {
        return new AgentStatusRequestExecutor(this, pluginRequest);
    }

    public static class Agent {
        @SerializedName("uuid")
        private String uuid;

        @SerializedName("host_name")
        private String hostName;

        @SerializedName("is_elastic")
        private boolean isElastic;

        @SerializedName("ip_address")
        private String ipAddress;

        @SerializedName("operating_system")
        private String operatingSystem;

        @SerializedName("free_space")
        private String freeSpace;

        @SerializedName("agent_config_state")
        private String configState;

        @SerializedName("transition_time")
        private String transitionTime;

        @SerializedName("build_state")
        private String buildState;

        @SerializedName("agent_state")
        private String agentState;
    }
}
