import db.Db;
import server.Routes;
import server.Server;

public class Main {

  public static void main(String[] args) throws Exception {
    Db.init();
    Routes.register();
    new Server(4000, "public").run();
  }
}
