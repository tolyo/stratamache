package server;

public record RouteDef(HandlerType method, String path, RouteHandle handler) {}
