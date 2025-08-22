package handlers;

import io.netty.handler.codec.http.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import server.HtmlResponse;
import server.RouteResponse;

import static j2html.TagCreator.*;

public class HelloHandler {
	
	public static final String URL = "/hello";

    public static RouteResponse get(ChannelHandlerContext ctx, FullHttpRequest request) {
    	return new HtmlResponse(
			div(
			   h1("Hello, World!")
			), 
			HttpResponseStatus.OK);
    }
}
