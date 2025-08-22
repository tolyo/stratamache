import server.Routes;
import server.Server;

public class Main {

  public static void main(String[] args) throws Exception {
    Routes.register();
    new Server(4000, "public").run();
  }
}
