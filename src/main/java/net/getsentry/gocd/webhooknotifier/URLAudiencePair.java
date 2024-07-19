package net.getsentry.gocd.webhooknotifier;

import java.net.URL;

/**
 * A pair of URL and audience where the audience is optional.
 * If the audience is not provided, it will be used to authenticate with the GCP metadata server.
 */
public class URLAudiencePair {
  private URL url;
  private String audience;

  public URLAudiencePair(String url, String audience) {
    this.url = validateURL(url);
    this.audience = audience;
  }

  public URLAudiencePair(String url) {
    this(url, null);
  }

  public URL getUrl() {
    return url;
  }

  public String getAudience() {
    return audience;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    URLAudiencePair that = (URLAudiencePair) o;

    if (url != null ? !url.equals(that.url) : that.url != null) return false;
    return audience != null ? audience.equals(that.audience) : that.audience == null;
  }

  @Override
  public int hashCode() {
    int result = url != null ? url.hashCode() : 0;
    result = 31 * result + (audience != null ? audience.hashCode() : 0);
    return result;
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
