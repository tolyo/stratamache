package game;

public class Position {

  int x;
  int y;

  Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public void update(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
