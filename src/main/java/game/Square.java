package game;

public class Square {

  final int x;
  final int y;
  boolean passable = true;
  boolean startable = false;
  Side side = Side.NEITHER;
  Piece piece;

  Square(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
