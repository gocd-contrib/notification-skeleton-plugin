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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;

public class PluginSettingsTest {
    @Test
    public void shouldFailToDeserializeHttp() throws Exception {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                "\"webhooks\": \"http://api.example.com \n     \n https://api-2.example.com \"" +
                "}");

        assertThat(
                "WebHooks",
                pluginSettings.getWebhooks(),
                arrayContainingInAnyOrder(
                        new URLAudiencePair("https://api-2.example.com")));
    }

    @Test
    public void shouldDeserializeFromJSON() throws Exception {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                "\"webhooks\": \"https://api.example.com \n     \n https://api-2.example.com \"" +
                "}");

        assertThat(
                "WebHooks",
                pluginSettings.getWebhooks(),
                arrayContainingInAnyOrder(
                        new URLAudiencePair("https://api.example.com"),
                        new URLAudiencePair("https://api-2.example.com")));
    }

    @Test
    public void shouldDeserializeFromJSONWithAudience() throws Exception {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                "\"webhooks\": \"https://api.example.com,audience1 \n     \n https://api-2.example.com,audience2 \"" +
                "}");

        assertThat(
                "WebHooks",
                pluginSettings.getWebhooks(),
                arrayContainingInAnyOrder(
                        new URLAudiencePair("https://api.example.com", "audience1"),
                        new URLAudiencePair("https://api-2.example.com", "audience2")));
    }
}
