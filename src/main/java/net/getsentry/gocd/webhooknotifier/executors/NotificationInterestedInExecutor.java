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

package net.getsentry.gocd.webhooknotifier.executors;

import net.getsentry.gocd.webhooknotifier.RequestExecutor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import static net.getsentry.gocd.webhooknotifier.Request.REQUEST_AGENT_STATUS;
import static net.getsentry.gocd.webhooknotifier.Request.REQUEST_STAGE_STATUS;

/**
 * This executor tells GoCD that we are interested in stage and agent
 * status updates.
 */
public class NotificationInterestedInExecutor implements RequestExecutor {
    private static final Gson GSON = new Gson();

    @Override
    public GoPluginApiResponse execute() throws Exception {
        JsonObject jsonObject = new JsonObject();
        JsonArray notifications = new JsonArray();
        notifications.add(REQUEST_STAGE_STATUS.requestName());
        notifications.add(REQUEST_AGENT_STATUS.requestName());
        jsonObject.add("notifications", notifications);

        DefaultGoPluginApiResponse defaultGoPluginApiResponse = new DefaultGoPluginApiResponse(200);
        defaultGoPluginApiResponse.setResponseBody(GSON.toJson(jsonObject));
        return defaultGoPluginApiResponse;
    }
}
