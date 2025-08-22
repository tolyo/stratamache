package server;

import java.util.ArrayList;
import java.util.List;

public class RouteRegistry {
  private static final List<RouteEntry> routes = new ArrayList<>();

  public static void addRoute(RouteDef routeDef) {
    routes.add(new RouteEntry(routeDef.method(), routeDef.path(), routeDef.handler()));
  }

  public static RouteHandle match(HandlerType method, String path) {
    for (RouteEntry entry : routes) {
      if (entry.method() == method && entry.path().equals(path)) {
        return entry.handler();
      }
    }
    return null;
  }
}
