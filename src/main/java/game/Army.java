package game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Army {

  public List<Piece> pieces;
  Side side;

  public Army(Side side) {
    this.side = side;
    pieces = new ArrayList<>();
    pieces.add(new Piece(10)); // marshal
    pieces.add(new Piece(9)); // general
    pieces.addAll(makePieces(8, 2)); // colonel
    pieces.addAll(makePieces(7, 3)); // major
    pieces.addAll(makePieces(6, 4)); // captain
    pieces.addAll(makePieces(5, 4)); // lieutenant
    pieces.addAll(makePieces(4, 4)); // sergeant
    pieces.addAll(makePieces(3, 5)); // miner
    pieces.addAll(makePieces(2, 8)); // scout
    pieces.add(new Piece(1)); // spy
    pieces.addAll(makePieces(0, 6)); // bomb
    pieces.add(new Piece(-1)); // flag

    // Mark as belonging to army
    pieces.forEach(x -> x.side = this.side);
  }

  private List<Piece> makePieces(int rank, int count) {
    return IntStream.range(0, count).mapToObj(i -> new Piece(rank)).collect(Collectors.toList());
  }

  public Piece findAnyByRank(int x) {
    return this.pieces.stream().filter(p -> p.rank == x).findAny().get();
  }
}
