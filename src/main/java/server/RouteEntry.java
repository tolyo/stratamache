package server;

public record RouteEntry(HandlerType method, String path, RouteHandle handler) {}
