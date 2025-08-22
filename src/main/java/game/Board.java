package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Board {
  public static final int WIDTH = 10;
  public static final int HEIGHT = 10;

  private static final int[][] LAKE_POSITIONS = {
    {2, 4}, {2, 5}, {3, 4}, {3, 5}, // Left lake block
    {6, 4}, {6, 5}, {7, 4}, {7, 5} // Right lake block
  };

  private final Square[][] grid;

  Army A;
  Army B;

  public Board() {
    grid = new Square[WIDTH][HEIGHT];

    // Initialize all squares
    for (int x = 0; x < WIDTH; x++) {
      for (int y = 0; y < HEIGHT; y++) {
        Square s = new Square(x, y);

        switch (y) {
          case 0, 1, 3, 4 -> {
            s.startable = true;
            s.side = Side.A;
          }
          case 6, 7, 8, 9 -> {
            s.startable = true;
            s.side = Side.B;
          }
          default -> {}
        }
        grid[x][y] = s;
      }
    }

    // Mark lake positions as impassable
    for (int[] pos : LAKE_POSITIONS) {
      int x = pos[0];
      int y = pos[1];
      if (isInBounds(x, y)) {
        grid[x][y].passable = false;
      }
    }

    A = new Army(Side.A);
    B = new Army(Side.B);
  }

  public static boolean isLakeTile(int x, int y) {
    for (int[] pos : LAKE_POSITIONS) {
      if (pos[0] == x && pos[1] == y) {
        return true;
      }
    }
    return false;
  }

  List<Square> validMoves(Piece piece) {
    Square location = this.grid[piece.position.x][piece.position.y];
    List<Square> validSquares = new ArrayList<>();

    int[][] directions = {
      {-1, 0}, // Up
      {1, 0}, // Down
      {0, -1}, // Left
      {0, 1} // Right
    };

    for (int[] dir : directions) {
      int dx = dir[0];
      int dy = dir[1];

      int newX = location.x + dx;
      int newY = location.y + dy;

      if (piece.rank == Piece.SCOUT) {
        // Keep going in this direction
        while (isInBounds(newX, newY) && isPassable(newX, newY)) {
          if (isOccupied(piece.side, newX, newY)) {
            break; // Can't move into or beyond own piece
          }

          validSquares.add(this.grid[newX][newY]);

          if (isOccupiedByOpposite(piece.side, newX, newY)) {
            validSquares.add(this.grid[newX][newY]);
            break; // Can capture, but can't move further
          }

          newX += dx;
          newY += dy;
        }
      } else {
        // One-step move for normal pieces
        if (isInBounds(newX, newY)
            && isPassable(newX, newY)
            && (!isOccupied(piece.side, newX, newY)
                || isOccupiedByOpposite(piece.side, newX, newY))) {
          validSquares.add(this.grid[newX][newY]);
        }
      }
    }

    return validSquares;
  }

  boolean isValidMove(Piece piece, int x, int y) {
    return validMoves(piece).stream().anyMatch(s -> s.x == x && s.y == y);
  }

  boolean isPassable(int x, int y) {
    Square square = this.grid[x][y];
    return square.passable;
  }

  Optional<Piece> getPiece(int x, int y) {
    Square square = this.grid[x][y];
    return Optional.ofNullable(square.piece);
  }

  boolean isOccupied(Side side, int x, int y) {
    return getPiece(x, y).map(piece -> piece.side == side).orElse(false);
  }

  boolean isOccupiedByOpposite(Side side, int x, int y) {
    return getPiece(x, y).map(piece -> piece.side == Side.getOpposite(side)).orElse(false);
  }

  boolean place(Piece piece, int x, int y) {
    if (!isInBounds(x, y)) {
      return false;
    }
    if (piece.position != null && piece.position.x == x && piece.position.y == y) {
      return false;
    }

    if (grid[x][y].side != piece.side) {
      return false;
    }

    if (grid[x][y].piece != null) {
      return false;
    }

    if (piece.position != null) {
      // clear the spot
      grid[piece.position.x][piece.position.y].piece = null;
    }

    grid[x][y].piece = piece;
    piece.position = new Position(x, y);
    return true;
  }

  // Movement may result in combat
  MovementResult move(Piece piece, int x, int y) {
    MovementResult res = new MovementResult();
    if (!piece.movable() || !isInBounds(x, y) || !isValidMove(piece, x, y)) {
      res.moved = false;
      return res;
    }

    if (isOccupiedByOpposite(piece.side, x, y)) {
      Piece enemy = grid[x][y].piece;
      Engagement engagement = piece.attack(enemy);
      switch (engagement) {
        case Engagement.WIN:
          killPiece(enemy);
          movePiece(piece, x, y);
          break;
        case Engagement.LOSE:
          killPiece(piece);
          break;
        case Engagement.DRAW:
          killPiece(piece);
          killPiece(enemy);
        default:
          break;
      }
      res.engagement = engagement;
    } else {
      grid[piece.position.x][piece.position.y].piece = null;
      piece.position.update(x, y);
      grid[x][y].piece = piece;
    }

    return res;
  }

  private void killPiece(Piece piece) {
    piece.alive = false;
    grid[piece.position.x][piece.position.y].piece = null;
    piece.position = null;
  }

  private void movePiece(Piece piece, int x, int y) {
    grid[piece.position.x][piece.position.y].piece = null;
    piece.position.update(x, y);
    grid[x][y].piece = piece;
  }

  private boolean isInBounds(int x, int y) {
    return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
  }
}
