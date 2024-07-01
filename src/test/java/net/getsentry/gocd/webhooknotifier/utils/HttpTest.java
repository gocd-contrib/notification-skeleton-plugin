package net.getsentry.gocd.webhooknotifier.utils;

import org.junit.Test;

import net.getsentry.gocd.webhooknotifier.PluginRequest;
import net.getsentry.gocd.webhooknotifier.PluginSettings;
import net.getsentry.gocd.webhooknotifier.ServerRequestFailedException;

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
import java.net.URI;
import java.net.URISyntaxException;

public class HttpTest {
    @Test
    public void testPost() throws UnsupportedEncodingException, IOException {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockClient.execute(any())).thenReturn(mockResponse);

        HttpResponse response = Http.post("https://example.com", "fakeData", mockClient);

        verify(mockClient).execute(any(HttpPost.class));
        assertThat(response, is(mockResponse));
    }

    @Test
    public void testGetAuthTokenException() throws URISyntaxException, IOException {
        HttpClient mockClient = mock(HttpClient.class);
        when(mockClient.execute(HttpGet.class.cast(any()))).thenThrow(new IOException());

        String authToken = Http.getAuthToken(new URI("https://user%40example.com@example.com"), mockClient);

        assertThat(authToken, is(nullValue()));
    }

    @Test
    public void testGetAuthToken() throws URISyntaxException, IOException {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse mockResponse = mock(HttpResponse.class);
        HttpEntity mockHttpEntity = mock(HttpEntity.class);

        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(mockHttpEntity.getContent()).thenReturn(new ByteArrayInputStream("fakeToken".getBytes()));
        when(mockClient.execute(any())).thenReturn(mockResponse);

        String authToken = Http.getAuthToken(new URI("https://user%40example.com@example.com"), mockClient);

        verify(mockClient).execute(argThat((HttpGet request) -> {
            try {
                return request.getURI().toString().equals(Http.GCP_AUTH_METADATA_URL + "https://example.com");
            } catch (Exception e) {
                return false;
            }
        }));
        assertThat(authToken, is("fakeToken"));
    }

    @Test
    public void testPingWebhooksFirstInstall() throws URISyntaxException, IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        when(mockPluginRequest.getPluginSettings()).thenReturn(null);

        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData", mockClient);

        // No requests should be made if the plugin settings are null
        verify(mockClient, never()).execute(any());
    }

    @Test
    public void testPingWebhooksWithAuthToken() throws URISyntaxException, IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        when(mockPluginSettings.getWebhookURIs()).thenReturn(new URI[] { new URI("https://user%40example.com@example.com") });
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
                    return getRequest.getURI().toString().equals(Http.GCP_AUTH_METADATA_URL + "https://example.com");
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
    public void testPingWebhooks() throws URISyntaxException, IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        when(mockPluginSettings.getWebhookURIs()).thenReturn(new URI[] { new URI("https://example.com") });
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
    public void testPingWebhooksMultiple() throws URISyntaxException, IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        when(mockPluginSettings.getWebhookURIs()).thenReturn(new URI[] { new URI("https://example.com"), new URI("https://example2.com") });
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
    public void testPingWebhooksException() throws URISyntaxException, IOException, ServerRequestFailedException {
        HttpClient mockClient = mock(HttpClient.class);
        PluginRequest mockPluginRequest = mock(PluginRequest.class);
        PluginSettings mockPluginSettings = mock(PluginSettings.class);
        when(mockPluginSettings.getWebhookURIs()).thenReturn(new URI[] { new URI("https://example.com") });
        when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);
        doThrow(new IOException()).when(mockClient).execute(any());

        Http.pingWebhooks(mockPluginRequest, "stage", "fakeData", mockClient);

        verify(mockClient).execute(any());
    }
}
