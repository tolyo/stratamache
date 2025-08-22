package server;

import handlers.HelloHandler;

public class Routes {
  public static void register() {

    RouteDef[] routes =
        new RouteDef[] {
          new RouteDef(HandlerType.GET, HelloHandler.URL, HelloHandler::get),
        };

    for (RouteDef def : routes) {
      RouteRegistry.addRoute(def);
    }
  }
}
