package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

@FunctionalInterface
public interface RouteHandle {
  RouteResponse handle(ChannelHandlerContext ctx, FullHttpRequest request);
}
