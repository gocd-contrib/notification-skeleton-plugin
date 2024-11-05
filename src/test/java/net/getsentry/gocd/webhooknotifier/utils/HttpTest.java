package net.getsentry.gocd.webhooknotifier.utils;

import org.junit.Test;

import net.getsentry.gocd.webhooknotifier.PluginRequest;
import net.getsentry.gocd.webhooknotifier.PluginSettings;
import net.getsentry.gocd.webhooknotifier.ServerRequestFailedException;
import net.getsentry.gocd.webhooknotifier.URLWithAuth;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class HttpTest {
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
        when(mockPluginSettings.getWebhooks()).thenReturn(new URLWithAuth[] { new URLWithAuth("https://example.com", "fakeAudience") });
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
                    return postRequest.getURI().toString().equals("https://example.com") && postRequest.getHeaders("Authorization")[0].getValue().equals("Bearer fakeToken");
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }));
    }

    @Test
    public void testPingWebhooks() throws IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        when(mockPluginSettings.getWebhooks()).thenReturn(new URLWithAuth[] { new URLWithAuth("https://example.com") });
        when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);

        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData", mockClient);

        verify(mockClient).execute(argThat((HttpPost request) -> {
            try {
                return request.getURI().toString().equals("https://example.com");
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
        when(mockPluginSettings.getWebhooks()).thenReturn(new URLWithAuth[] { new URLWithAuth("https://example.com"), new URLWithAuth("https://example2.com") });
        when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);

        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData", mockClient);

        verify(mockClient, times(2)).execute(argThat((HttpPost request) -> {
            try {
                return request.getURI().toString().equals("https://example.com") || request.getURI().toString().equals("https://example2.com");
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
        when(mockPluginSettings.getWebhooks()).thenReturn(new URLWithAuth[] { new URLWithAuth("https://example.com", null, "webhooksecret") });
        when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);

        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData", mockClient);

        verify(mockClient).execute(argThat((HttpPost request) -> {
            try {
                return request.getURI().toString().equals("https://example.com") && request.getHeaders("x-gocd-signature")[0].getValue().equals("64b0880a3e39c6dc74ab0a3db5b7fc7efc6dd8b70e3cb026d6af0b74b8b183c6");
            } catch (Exception e) {
                return false;
            }
        }));
    }
}
