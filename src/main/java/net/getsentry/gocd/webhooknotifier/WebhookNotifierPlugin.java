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

import net.getsentry.gocd.webhooknotifier.executors.GetPluginConfigurationExecutor;
import net.getsentry.gocd.webhooknotifier.executors.GetSettingsViewRequestExecutor;
import net.getsentry.gocd.webhooknotifier.executors.NotificationInterestedInExecutor;
import net.getsentry.gocd.webhooknotifier.requests.AgentStatusRequest;
import net.getsentry.gocd.webhooknotifier.requests.StageStatusRequest;
import net.getsentry.gocd.webhooknotifier.requests.ValidatePluginSettings;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import static net.getsentry.gocd.webhooknotifier.Constants.PLUGIN_IDENTIFIER;

@Extension
public class WebhookNotifierPlugin implements GoPlugin {

    public static final Logger LOG = Logger.getLoggerFor(WebhookNotifierPlugin.class);

    private GoApplicationAccessor accessor;
    private PluginRequest pluginRequest;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        this.accessor = accessor;
        this.pluginRequest = new PluginRequest(accessor);
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        try {
            System.out.printf("😺 WebhookNotifierPlugin->handle() %s\n", request.requestName());
            switch (Request.fromString(request.requestName())) {
                case PLUGIN_SETTINGS_GET_VIEW:
                    return new GetSettingsViewRequestExecutor().execute();
                case REQUEST_NOTIFICATIONS_INTERESTED_IN:
                    return new NotificationInterestedInExecutor().execute();
                case PLUGIN_SETTINGS_GET_CONFIGURATION:
                    return new GetPluginConfigurationExecutor().execute();
                case PLUGIN_SETTINGS_VALIDATE_CONFIGURATION:
                    return ValidatePluginSettings.fromJSON(request.requestBody()).executor().execute();
                // The following cases that call the webhook
                case REQUEST_STAGE_STATUS:
                    return StageStatusRequest.fromJSON(request.requestBody()).executor(pluginRequest).execute();
                case REQUEST_AGENT_STATUS:
                    return AgentStatusRequest.fromJSON(request.requestBody()).executor(pluginRequest).execute();
                default:
                    throw new UnhandledRequestTypeException(request.requestName());
            }
        } catch (Exception e) {
            LOG.error("Failed to refresh configuration", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }
}
