package server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

public class RouterHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String path = request.uri().split("\\?")[0]; // Strip query params
        HandlerType method;

        try {
            method = HandlerType.valueOf(request.method().name());
        } catch (IllegalArgumentException e) {
            sendResponse(ctx, new RouteResponse("Unsupported HTTP method", "text/plain", HttpResponseStatus.METHOD_NOT_ALLOWED));
            return;
        }

        RouteHandle handler = RouteRegistry.match(method, path);

        if (handler != null) {
            RouteResponse response = handler.handle(ctx, request);
            sendResponse(ctx, response);
        } else {
        	// Retain and pass the request along the pipeline
            ctx.fireChannelRead(request.retain());
        }
    }

    private void sendResponse(ChannelHandlerContext ctx, RouteResponse response) {
        byte[] content = response.body().getBytes();
        FullHttpResponse nettyResponse = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            response.status(),
            Unpooled.wrappedBuffer(content)
        );

        nettyResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, response.contentType());
        nettyResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);

        ctx.writeAndFlush(nettyResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
