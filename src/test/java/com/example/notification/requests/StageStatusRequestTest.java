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

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class StageStatusRequestTest {

    @Test
    public void shouldDeserializeFromJSONWithoutLoosingAnyData() throws Exception {
        String json = "{\n" +
                "  \"pipeline\": {\n" +
                "    \"name\": \"pipeline-name\",\n" +
                "    \"counter\": \"1\",\n" +
                "    \"group\": \"pipeline-group\",\n" +
                "    \"build-cause\": [\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"git-configuration\": {\n" +
                "            \"shallow-clone\": false,\n" +
                "            \"branch\": \"branch\",\n" +
                "            \"url\": \"http://user:******@gitrepo.com\"\n" +
                "          },\n" +
                "          \"type\": \"git\"\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317Z\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"mercurial-configuration\": {\n" +
                "            \"url\": \"http://user:******@hgrepo.com\"\n" +
                "          },\n" +
                "          \"type\": \"mercurial\"\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317Z\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"svn-configuration\": {\n" +
                "            \"username\": \"username\",\n" +
                "            \"check-externals\": false,\n" +
                "            \"url\": \"http://user:******@svnrepo.com\"\n" +
                "          },\n" +
                "          \"type\": \"svn\"\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317Z\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"tfs-configuration\": {\n" +
                "            \"username\": \"username\",\n" +
                "            \"project-path\": \"project-path\",\n" +
                "            \"domain\": \"domain\",\n" +
                "            \"url\": \"http://user:******@tfsrepo.com\"\n" +
                "          },\n" +
                "          \"type\": \"tfs\"\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317Z\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"perforce-configuration\": {\n" +
                "            \"username\": \"username\",\n" +
                "            \"use-tickets\": false,\n" +
                "            \"view\": \"view\",\n" +
                "            \"url\": \"127.0.0.1:1666\"\n" +
                "          },\n" +
                "          \"type\": \"perforce\"\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317Z\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"pipeline-configuration\": {\n" +
                "            \"pipeline-name\": \"pipeline-name\",\n" +
                "            \"stage-name\": \"stage-name\"\n" +
                "          },\n" +
                "          \"type\": \"pipeline\"\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"pipeline-name/1/stage-name/1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317Z\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"plugin-id\": \"pluginid\",\n" +
                "          \"package-configuration\": {\n" +
                "            \"k3\": \"package-v1\"\n" +
                "          },\n" +
                "          \"type\": \"package\",\n" +
                "          \"repository-configuration\": {\n" +
                "            \"k1\": \"repo-v1\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317Z\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"plugin-id\": \"pluginid\",\n" +
                "          \"type\": \"scm\",\n" +
                "          \"scm-configuration\": {\n" +
                "            \"k1\": \"v1\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317Z\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"1\",\n" +
                "      \"approval-type\": \"success\",\n" +
                "      \"approved-by\": \"changes\",\n" +
                "      \"state\": \"Passed\",\n" +
                "      \"result\": \"Passed\",\n" +
                "      \"create-time\": \"2011-07-13T19:43:37.100Z\",\n" +
                "      \"last-transition-time\": \"2011-07-13T19:43:37.100Z\",\n" +
                "      \"jobs\": [\n" +
                "        {\n" +
                "          \"name\": \"job-name\",\n" +
                "          \"schedule-time\": \"2011-07-13T19:43:37.100Z\",\n" +
                "          \"complete-time\": \"2011-07-13T19:43:37.100Z\",\n" +
                "          \"state\": \"Completed\",\n" +
                "          \"result\": \"Passed\",\n" +
                "          \"agent-uuid\": \"uuid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        StageStatusRequest request = StageStatusRequest.fromJSON(json);
        String serializedAgain = StageStatusRequest.GSON.toJson(request);
        JSONAssert.assertEquals(json, serializedAgain, true);
    }
}
