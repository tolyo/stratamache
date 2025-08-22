package server;


import io.netty.handler.codec.http.HttpResponseStatus;


public class TextResponse extends RouteResponse {
    public TextResponse(String body, HttpResponseStatus status) {
        super(body, "text/plain", status);
    }
}
