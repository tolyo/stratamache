package game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ArmyTest {

  @Test
  void testSize() {
    assertEquals(40, new Army(Side.NEITHER).pieces.size());
  }

  @Test
  void testSideOfPieces() {
    assertEquals(Side.A, new Army(Side.A).pieces.getFirst().side);
    assertEquals(Side.B, new Army(Side.B).pieces.getFirst().side);
  }
}
