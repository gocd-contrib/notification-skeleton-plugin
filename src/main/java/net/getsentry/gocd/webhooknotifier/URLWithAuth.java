package net.getsentry.gocd.webhooknotifier;

import java.net.URL;

/**
 * A pair of URL and audience where the audience is optional.
 * If the audience is not provided, it will be used to authenticate with the GCP metadata server.
 */
public class URLWithAuth {
  private URL url;
  private String audience;
  private String secretValue;

  public URLWithAuth(String url, String audience, String secretValue) {
    this.url = validateURL(url);
    this.audience = audience;
    this.secretValue = secretValue;
  }

  public URLWithAuth(String url, String audience) {
    this(url, audience, null);
  }

  public URLWithAuth(String url) {
    this(url, null);
  }

  public URL getUrl() {
    return url;
  }

  public String getAudience() {
    return audience;
  }

  public String getSecretValue() {
    return secretValue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    result = prime * result + ((audience == null) ? 0 : audience.hashCode());
    result = prime * result + ((secretValue == null) ? 0 : secretValue.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    URLWithAuth other = (URLWithAuth) obj;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    if (audience == null) {
      if (other.audience != null)
        return false;
    } else if (!audience.equals(other.audience))
      return false;
    if (secretValue == null) {
      if (other.secretValue != null)
        return false;
    } else if (!secretValue.equals(other.secretValue))
      return false;
    return true;
  }

  private URL validateURL(String url) {
    try {
      URL parsedUrl = new URL(url);
      if (!parsedUrl.getProtocol().equals("https")) {
        throw new IllegalArgumentException("Only HTTPS URLs are supported");
      } else {
        return parsedUrl;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid URL: " + url);
    }
  }
}
