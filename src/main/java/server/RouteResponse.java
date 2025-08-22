package server;

import io.netty.handler.codec.http.HttpResponseStatus;

public class RouteResponse {
  private final String body;
  private final String contentType;
  private final HttpResponseStatus status;

  public RouteResponse(String body, String contentType, HttpResponseStatus status) {
    this.body = body;
    this.contentType = contentType;
    this.status = status;
  }

  public String body() {
    return body;
  }

  public String contentType() {
    return contentType;
  }

  public HttpResponseStatus status() {
    return status;
  }
}
