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

package net.getsentry.gocd.webhooknotifier.requests;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class AgentStatusRequestTest {

    @Test
    public void shouldDeserializeFromJSONWithoutLoosingAnyData() throws Exception {
        String json = "{\n" +
                "  \"agent\": {\n" +
                "    \"uuid\": \"uuid\",\n" +
                "    \"host_name\": \"hostname\",\n" +
                "    \"is_elastic\": true,\n" +
                "    \"ip_address\": \"1.1.1.1\",\n" +
                "    \"operating_system\": \"windows 2008\",\n" +
                "    \"free_space\": \"100 bytes\",\n" +
                "    \"agent_config_state\": \"enabled\",\n" +
                "    \"agent_state\": \"idle\",\n" +
                "    \"build_state\": \"idle\",\n" +
                "    \"transition_time\": \"2016-04-06T12:50:03.317+0000\"\n" +
                "  }\n" +
                "}";

        AgentStatusRequest request = AgentStatusRequest.fromJSON(json);
        String serializedAgain = AgentStatusRequest.GSON.toJson(request);
        JSONAssert.assertEquals(json, serializedAgain, true);
    }
}
