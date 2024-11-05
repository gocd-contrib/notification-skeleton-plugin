package net.getsentry.gocd.webhooknotifier.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import net.getsentry.gocd.webhooknotifier.URLWithAuth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthTest {
    @Test
    public void testGetAuthTokenException() throws IOException {
        HttpClient mockClient = mock(HttpClient.class);
        when(mockClient.execute(HttpGet.class.cast(any()))).thenThrow(new IOException());

        String authToken = Auth.getAuthToken(new URLWithAuth("https://example.com", "fakeAudience"), mockClient);

        assertThat(authToken, is(nullValue()));
    }

    @Test
    public void testGetAuthToken() throws IOException {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse mockResponse = mock(HttpResponse.class);
        HttpEntity mockHttpEntity = mock(HttpEntity.class);

        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(mockHttpEntity.getContent()).thenReturn(new ByteArrayInputStream("fakeToken".getBytes()));
        when(mockClient.execute(any())).thenReturn(mockResponse);

        String authToken = Auth.getAuthToken(new URLWithAuth("https://example.com", "fakeAudience"), mockClient);

        verify(mockClient).execute(argThat((HttpGet request) -> {
            try {
                return request.getURI().toString().equals(Http.GCP_AUTH_METADATA_URL + "fakeAudience");
            } catch (Exception e) {
                return false;
            }
        }));
        assertThat(authToken, is("fakeToken"));
    }

    @Test
    public void testCreateSignature() throws NoSuchAlgorithmException {
        String signature = Auth.createSignature("test", "webhooksecret");
        assertThat(signature, is("d01e0bee5d3b7783715126071e1c94df60bee516b4d6b1cb92896265cbbfe894"));
    }
}
