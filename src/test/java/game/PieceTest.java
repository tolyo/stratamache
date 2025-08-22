package game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PieceTest {

	@Test
	void testConstructor() {
		Piece piece = new Piece(2);
		assertTrue(piece.rank == 2); 
	}
	
	@Test 
	void testAttack() {
		Piece piece1 = new Piece(1); // spy
		Piece piece2 = new Piece(2); // scout
		Piece piece3 = new Piece(3); // miner
		Piece piece10 = new Piece(10); // marshal
		
		assertTrue(piece1.attack(piece2) == Engagement.LOSE);
		assertTrue(piece2.attack(piece1) == Engagement.WIN);

		assertTrue(piece2.attack(piece3) == Engagement.LOSE);
		assertTrue(piece3.attack(piece1) == Engagement.WIN);

		assertTrue(piece1.attack(piece3) == Engagement.LOSE);
		assertTrue(piece3.attack(piece2) == Engagement.WIN);
		
		assertTrue(piece3.attack(piece3) == Engagement.DRAW);
		
		// non-moving pieces
		assertThrows(RuntimeException.class, () -> new Piece(Piece.BOMB).attack(piece3));
		assertThrows(RuntimeException.class, () -> new Piece(Piece.FLAG).attack(piece3));
		
		// bomb mechanics
		assertTrue(piece3.attack(new Piece(Piece.BOMB)) == Engagement.WIN);
		assertTrue(piece1.attack(new Piece(Piece.BOMB)) == Engagement.LOSE);
		assertTrue(piece1.attack(piece10) == Engagement.WIN);
	}
	
	@Test
	void testMovable() {
		assertTrue(new Piece(1).movable());
		
		// Bombs and flags cannot move
		assertFalse(new Piece(0).movable());
		assertFalse(new Piece(-1).movable());
	}

}
