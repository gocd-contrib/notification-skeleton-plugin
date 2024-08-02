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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.protobuf.ByteString;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class PluginSettingsTest {
        private SecretManagerServiceClient mockClient;

        @Before
        public void setUp() {
                mockClient = mock(SecretManagerServiceClient.class);
        }

        @After
        public void tearDown() {
                reset(mockClient);
        }

        @Test
        public void shouldFailToDeserializeHttp() throws Exception {
                PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                                "\"webhooks\": \"http://api.example.com \n     \n https://api-2.example.com \"" +
                                "}");

                assertThat(
                                "WebHooks",
                                pluginSettings.getWebhooks(),
                                arrayContainingInAnyOrder(
                                                new URLWithAuth("https://api-2.example.com")));
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
                                                new URLWithAuth("https://api.example.com"),
                                                new URLWithAuth("https://api-2.example.com")));
        }

        @Test
        public void shouldDeserializeFromJSONWithAudience() throws Exception {
                PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                                "\"webhooks\": \"https://api.example.com,audience1 \n     \n https://api-2.example.com,audience2 \""
                                +
                                "}");

                assertThat(
                                "WebHooks",
                                pluginSettings.getWebhooks(),
                                arrayContainingInAnyOrder(
                                                new URLWithAuth("https://api.example.com", "audience1"),
                                                new URLWithAuth("https://api-2.example.com", "audience2")));
        }

        @Test
        public void shouldDeserializeFromJSONWithGCPSecret() throws Exception {
                when(mockClient.accessSecretVersion(any(String.class)))
                                .thenReturn(AccessSecretVersionResponse.newBuilder().setName("secret1")
                                                .setPayload(SecretPayload.newBuilder()
                                                                .setData(ByteString.copyFromUtf8("supersecret")))
                                                .build());

                PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                                "\"webhooks\": \"https://api.example.com,gcp-secret:projects/123/secrets/secret1 \n     \n https://api-2.example.com \""
                                +
                                "}");

                assertThat(
                                "WebHooks",
                                pluginSettings.getWebhooks(mockClient),
                                arrayContainingInAnyOrder(
                                                new URLWithAuth("https://api.example.com", null, "supersecret"),
                                                new URLWithAuth("https://api-2.example.com", null)));
        }

        @Test
        public void shouldFailGracefullyIfSecretManagerFails() throws Exception {
                when(mockClient.accessSecretVersion(any(String.class)))
                                .thenThrow(new RuntimeException("Failed to fetch secret"));

                PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                                "\"webhooks\": \"https://api.example.com,gcp-secret:projects/123/secrets/secret2 \n     \n https://api-2.example.com \""
                                +
                                "}");

                assertThat(
                                "WebHooks",
                                pluginSettings.getWebhooks(mockClient),
                                arrayContainingInAnyOrder(
                                                new URLWithAuth("https://api.example.com", null),
                                                new URLWithAuth("https://api-2.example.com", null)));
        }

        @Test
        public void shouldFailGracefullyIfSecretManagerIsNotAvailable() throws Exception {
                PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                                "\"webhooks\": \"https://api.example.com,gcp-secret:projects/1233/secrets/secret3 \n     \n https://api-2.example.com \""
                                +
                                "}");

                assertThat(
                                "WebHooks",
                                pluginSettings.getWebhooks(null),
                                arrayContainingInAnyOrder(
                                                new URLWithAuth("https://api-2.example.com", null)));
        }
}
