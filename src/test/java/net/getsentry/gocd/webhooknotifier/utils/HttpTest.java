package net.getsentry.gocd.webhooknotifier.utils;

import net.getsentry.gocd.webhooknotifier.PluginRequest;
import net.getsentry.gocd.webhooknotifier.PluginSettings;
import net.getsentry.gocd.webhooknotifier.ServerRequestFailedException;
import net.getsentry.gocd.webhooknotifier.URLWithAuth;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class HttpTest {
    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @After
    public void tearDown() throws IOException {
        TimeZone.setDefault(TimeZone.getDefault());
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    private PluginRequest mockPluginRequestWithWebhooks(URLWithAuth... webhooks) throws ServerRequestFailedException {
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        doReturn(webhooks).when(mockPluginSettings).getWebhooks();
        doReturn(mockPluginSettings).when(mockPluginRequest).getPluginSettings();
        return mockPluginRequest;
    }

    private URLWithAuth mockUrl(String url, String audience, String secret) throws Exception {
        URLWithAuth mock = mock(URLWithAuth.class);
        when(mock.getUrl()).thenReturn(new URL(url));
        when(mock.getAudience()).thenReturn(audience);
        when(mock.getSecretValue()).thenReturn(secret);
        return mock;
    }

    @Test
    public void testPingWebhooksFirstInstall() throws ServerRequestFailedException {
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        when(mockPluginRequest.getPluginSettings()).thenReturn(null);
        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData");
        assertThat(mockWebServer.getRequestCount(), is(0));
    }

    @Test
    public void testPingWebhooks() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        URLWithAuth webhook = mockUrl(mockWebServer.url("/").toString(), null, null);
        PluginRequest request = mockPluginRequestWithWebhooks(webhook);
        Http.pingWebhooks(request, "stage", "fakeData");
        RecordedRequest recorded = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recorded, is(notNullValue()));
        assertThat(recorded.getMethod(), is("POST"));
        String body = recorded.getBody().readUtf8();
        assertThat(body, containsString("\"type\":\"stage\""));
        assertThat(body, containsString("\"data\":\"fakeData\""));
    }

    @Test
    public void testPingWebhooksMultiple() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        MockWebServer server2 = new MockWebServer();
        server2.start();
        server2.enqueue(new MockResponse().setResponseCode(200));
        URLWithAuth webhook1 = mockUrl(mockWebServer.url("/").toString(), null, null);
        URLWithAuth webhook2 = mockUrl(server2.url("/").toString(), null, null);
        PluginRequest request = mockPluginRequestWithWebhooks(webhook1, webhook2);
        Http.pingWebhooks(request, "stage", "fakeData");
        assertThat(mockWebServer.takeRequest(5, TimeUnit.SECONDS), is(notNullValue()));
        assertThat(server2.takeRequest(5, TimeUnit.SECONDS), is(notNullValue()));
        server2.shutdown();
    }

    @Test
    public void testPingWebhooksException() throws Exception {
        URLWithAuth webhook = mockUrl("http://localhost:9999/bad", null, null);
        PluginRequest request = mockPluginRequestWithWebhooks(webhook);
        Http.pingWebhooks(request, "stage", "fakeData");
        Thread.sleep(1000);
    }

    @Test
    public void testPingWebhooksWithSecret() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        URLWithAuth webhook = mockUrl(mockWebServer.url("/").toString(), null, "webhooksecret");
        PluginRequest request = mockPluginRequestWithWebhooks(webhook);
        Http.pingWebhooks(request, "stage", "fakeData");
        RecordedRequest recorded = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        String signature = recorded.getHeader("x-gocd-signature");
        assertThat(signature, is("64b0880a3e39c6dc74ab0a3db5b7fc7efc6dd8b70e3cb026d6af0b74b8b183c6"));
    }
}
