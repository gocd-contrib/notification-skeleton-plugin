/*
 * Copyright 2016 ThoughtWorks, Inc.
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

package com.example.notification.requests;

import com.example.notification.PluginRequest;
import com.example.notification.RequestExecutor;
import com.example.notification.executors.StageStatusRequestExecutor;
import com.example.notification.utils.DefaultDateTypeAdapter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class StageStatusRequest {
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new DefaultDateTypeAdapter(DATE_PATTERN))
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public Pipeline pipeline;

    public static StageStatusRequest fromJSON(String json) {
        return GSON.fromJson(json, StageStatusRequest.class);
    }

    public RequestExecutor executor(PluginRequest pluginRequest) {
        return new StageStatusRequestExecutor(this, pluginRequest);
    }

    public static class Pipeline {
        @SerializedName("name")
        public String name;

        @SerializedName("counter")
        public String counter;

        @SerializedName("group")
        public String group;

        @SerializedName("build-cause")
        public List<BuildCause> buildCause;

        @SerializedName("stage")
        public Stage stage;
    }

    public static class BuildCause {
        @SerializedName("material")
        public Map material;

        @SerializedName("changed")
        public Boolean changed;

        @SerializedName("modifications")
        public List<Modification> modifications;
    }

    public static class Stage {
        @SerializedName("name")
        public String name;

        @SerializedName("counter")
        public String counter;

        @SerializedName("approval-type")
        public String approvalType;

        @SerializedName("approved-by")
        public String approvedBy;

        @SerializedName("state")
        public String state;

        @SerializedName("result")
        public String result;

        @SerializedName("create-time")
        public Date createTime;

        @SerializedName("last-transition-time")
        public Date lastTransitionTime;
        public List<Job> jobs;
    }

    public static class Job {
        @SerializedName("name")
        public String name;

        @SerializedName("schedule-time")
        public Date scheduleTime;

        @SerializedName("complete-time")
        public Date completeTime;

        @SerializedName("state")
        public String state;

        @SerializedName("result")
        public String result;

        @SerializedName("agent-uuid")
        public String agentUuid;
    }

    public static class Modification {
        @SerializedName("revision")
        public String revision;

        @SerializedName("modified-time")
        public Date modifiedTime;

        @SerializedName("data")
        public Map data;
    }
}
