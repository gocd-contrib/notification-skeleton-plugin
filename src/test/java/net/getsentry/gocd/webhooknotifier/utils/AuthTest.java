package net.getsentry.gocd.webhooknotifier.utils;

import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import net.getsentry.gocd.webhooknotifier.URLWithAuth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthTest {
    @Test
    public void testGetAuthTokenException() {
        URLWithAuth urlWithAuth = new URLWithAuth("https://example.com", "fakeAudience");
        String authToken = Auth.getAuthToken(urlWithAuth);
        assertThat(authToken, is(nullValue()));
    }

    @Test
    public void testCreateSignature() throws NoSuchAlgorithmException {
        String signature = Auth.createSignature("test", "webhooksecret");
        assertThat(signature, is("d01e0bee5d3b7783715126071e1c94df60bee516b4d6b1cb92896265cbbfe894"));
    }
}
