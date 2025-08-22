package server;

import io.netty.handler.codec.http.HttpResponseStatus;
import j2html.tags.DomContent;

public class HtmlResponse extends RouteResponse {
  public HtmlResponse(DomContent body, HttpResponseStatus status) {
    super(body.render(), "text/html", status);
  }
}
