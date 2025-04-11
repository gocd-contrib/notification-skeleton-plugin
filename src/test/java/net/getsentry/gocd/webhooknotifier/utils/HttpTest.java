package net.getsentry.gocd.webhooknotifier.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.getsentry.gocd.webhooknotifier.PluginRequest;
import net.getsentry.gocd.webhooknotifier.PluginSettings;
import net.getsentry.gocd.webhooknotifier.ServerRequestFailedException;
import net.getsentry.gocd.webhooknotifier.URLWithAuth;
import net.getsentry.gocd.webhooknotifier.requests.StageStatusRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.ParseException;
import java.util.TimeZone;

public class HttpTest {

    @Before
    public void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(TimeZone.getDefault());
    }

    @Test
    public void testPost() throws UnsupportedEncodingException, IOException {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockClient.execute(any())).thenReturn(mockResponse);

        HttpResponse response = Http.post(new URL("https://example.com"), "fakeData", mockClient);

        verify(mockClient).execute(any(HttpPost.class));
        assertThat(response, is(mockResponse));
    }

    @Test
    public void testPingWebhooksFirstInstall() throws IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        when(mockPluginRequest.getPluginSettings()).thenReturn(null);

        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData", mockClient);

        // No requests should be made if the plugin settings are null
        verify(mockClient, never()).execute(any());
    }

    @Test
    public void testPingWebhooksWithAuthToken() throws IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        when(mockPluginSettings.getWebhooks())
                .thenReturn(new URLWithAuth[] { new URLWithAuth("https://example.com", "fakeAudience") });
        when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);

        HttpResponse mockGetResponse = mock(HttpResponse.class);
        HttpEntity mockGetEntity = mock(HttpEntity.class);
        when(mockGetResponse.getEntity()).thenReturn(mockGetEntity);
        when(mockGetEntity.getContent()).thenReturn(new ByteArrayInputStream("fakeToken".getBytes()));

        HttpResponse mockPostResponse = mock(HttpResponse.class);
        when(mockClient.execute(argThat(request -> request instanceof HttpGet))).thenReturn(mockGetResponse);
        when(mockClient.execute(argThat(request -> request instanceof HttpPost))).thenReturn(mockPostResponse);

        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData", mockClient);

        verify(mockClient).execute(argThat((request) -> {
            if (request instanceof HttpGet) {
                HttpGet getRequest = (HttpGet) request;
                try {
                    return getRequest.getURI().toString().equals(Http.GCP_AUTH_METADATA_URL + "fakeAudience");
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }));
        verify(mockClient).execute(argThat(request -> {
            if (request instanceof HttpPost) {
                HttpPost postRequest = (HttpPost) request;
                try {
                    return postRequest.getURI().toString().equals("https://example.com")
                            && postRequest.getHeaders("Authorization")[0].getValue().equals("Bearer fakeToken");
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }));
    }

    @Test
    public void testPingWebhooks() throws IOException, ServerRequestFailedException, ParseException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        when(mockPluginSettings.getWebhooks()).thenReturn(new URLWithAuth[] { new URLWithAuth("https://example.com") });
        when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);

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
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
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
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
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
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
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
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
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
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
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
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
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
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
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
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
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
                "      \"create-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"last-transition-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"jobs\": [\n" +
                "        {\n" +
                "          \"name\": \"job-name\",\n" +
                "          \"schedule-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "          \"complete-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "          \"state\": \"Completed\",\n" +
                "          \"result\": \"Passed\",\n" +
                "          \"agent-uuid\": \"uuid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
        StageStatusRequest stageStatusRequest = StageStatusRequest.fromJSON(json);

        Http.pingWebhooks(mockPluginRequest, "stage", stageStatusRequest, mockClient);

        verify(mockClient).execute(argThat((HttpPost request) -> {
            try {
                // Ensure the request is to the correct URL and the content formats the date correctly
                String content = EntityUtils.toString(request.getEntity());
                return request.getURI().toString().equals("https://example.com") && content.contains("\"complete-time\":\"2011-07-13T19:43:37.100+0000\"");
            } catch (Exception e) {
                return false;
            }
        }));
    }

    @Test
    public void testPingWebhooksMultiple() throws IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        when(mockPluginSettings.getWebhooks()).thenReturn(
                new URLWithAuth[] { new URLWithAuth("https://example.com"), new URLWithAuth("https://example2.com") });
        when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);

        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData", mockClient);

        verify(mockClient, times(2)).execute(argThat((HttpPost request) -> {
            try {
                return request.getURI().toString().equals("https://example.com")
                        || request.getURI().toString().equals("https://example2.com");
            } catch (Exception e) {
                return false;
            }
        }));
    }

    @Test
    public void testPingWebhooksException() throws IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        when(mockPluginSettings.getWebhooks()).thenReturn(new URLWithAuth[] { new URLWithAuth("https://example.com") });
        when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);
        doThrow(new IOException()).when(mockClient).execute(any());

        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData", mockClient);

        verify(mockClient).execute(any());
    }

    @Test
    public void testPingWebhooksWithSecret() throws IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        when(mockPluginSettings.getWebhooks())
                .thenReturn(new URLWithAuth[] { new URLWithAuth("https://example.com", null, "webhooksecret") });
        when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);

        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData", mockClient);

        verify(mockClient).execute(argThat((HttpPost request) -> {
            try {
                return request.getURI().toString().equals("https://example.com")
                        && request.getHeaders("x-gocd-signature")[0].getValue()
                                .equals("64b0880a3e39c6dc74ab0a3db5b7fc7efc6dd8b70e3cb026d6af0b74b8b183c6");
            } catch (Exception e) {
                return false;
            }
        }));
    }
}
