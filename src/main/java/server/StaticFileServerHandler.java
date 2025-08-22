package server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StaticFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private final String rootDir;

  public StaticFileServerHandler(String rootDir) {
    this.rootDir = rootDir;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
    if (!request.method().equals(HttpMethod.GET)) {
      sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
      return;
    }

    // Decode URI and prevent directory traversal
    String rawUri = request.uri();
    String uri = URLDecoder.decode(rawUri, StandardCharsets.UTF_8);

    // Default file
    if (uri.equals("/") || uri.isEmpty()) {
      uri = "/index.html";
    }

    // Sanitize path to prevent "../" access
    File file = new File(rootDir, uri).getCanonicalFile();

    if (!file.getPath().startsWith(new File(rootDir).getCanonicalPath())) {
      sendError(ctx, HttpResponseStatus.FORBIDDEN);
      return;
    }

    if (!file.exists() || file.isDirectory()) {
      sendError(ctx, HttpResponseStatus.NOT_FOUND);
      return;
    }

    try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
      long fileLength = raf.length();

      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
      String contentType = Files.probeContentType(Paths.get(file.getPath()));
      response
          .headers()
          .set(
              HttpHeaderNames.CONTENT_TYPE,
              contentType != null ? contentType : "application/octet-stream");
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileLength);

      ctx.write(response);
      ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength));
      ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }
  }

  private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
    FullHttpResponse response =
        new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            status,
            Unpooled.copiedBuffer(
                ("Failure: " + status + "\r\n").getBytes(StandardCharsets.UTF_8)));

    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }
}
