package game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class BoardTest {
    Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void testPieceCount() {
        assertEquals(40, board.A.pieces.size());
        assertEquals(40, board.B.pieces.size());
    }

    @Test
    void testValidPiecePlacement() {
        assertTrue(board.place(board.A.pieces.getFirst(), 0, 0));
        assertTrue(board.place(board.A.pieces.getLast(), 0, 1));
        assertTrue(board.place(board.B.pieces.getFirst(), 0, 6));
        assertTrue(board.place(board.B.pieces.getLast(), 0, 7));
    }

    @Test
    void testInvalidPos() {
        assertFalse(board.place(board.A.pieces.getFirst(), -10, 30));
        assertFalse(board.place(board.A.pieces.getFirst(), 0, -3));
        assertFalse(board.place(board.A.pieces.getFirst(), 8, 8));
    }

    @Test
    void testInValidPiecePlacement() {
        // A cannot place on B's side
        assertFalse(board.place(board.A.pieces.getFirst(), 0, 9));
        assertFalse(board.place(board.A.pieces.getFirst(), 1, 8));

        // B cannot place on A's side
        assertFalse(board.place(board.B.pieces.getFirst(), 0, 0));
        assertFalse(board.place(board.B.pieces.getLast(), 1, 1));

        // Neither can place in neutral zone (e.g. middle rows)
        assertFalse(board.place(board.A.pieces.getFirst(), 0, 5));
        assertFalse(board.place(board.B.pieces.getFirst(), 0, 5));
    }

    @Test
    void testDublicatePiecePlacement() {
        assertTrue(board.place(board.A.pieces.getFirst(), 0, 0));
        assertFalse(board.place(board.A.pieces.getFirst(), 0, 0));
        assertTrue(board.place(board.A.pieces.getFirst(), 0, 1));
    }

    @Test
    void testOccupiedPiecePlacement() {
        assertTrue(board.place(board.A.pieces.getFirst(), 0, 0));
        assertFalse(board.place(board.A.pieces.getLast(), 0, 0));

        assertTrue(board.place(board.A.pieces.getFirst(), 0, 1));
        assertTrue(board.place(board.A.pieces.getLast(), 0, 0));
    }

    @Test
    void testMovement() {
        board = new Board();
        Piece pieceA = board.A.pieces.getFirst();
        Piece bomb = board.A.findAnyByRank(Piece.BOMB);

        board.place(pieceA, 0, 0);
        board.place(bomb, 1, 0);

        assertFalse(board.move(pieceA, -1, 1).moved, "Cannot move outside bounds");
        assertFalse(board.move(bomb, 1, 1).moved, "Cannot move non-movables");

        assertTrue(board.move(pieceA, 0, 1).moved);
        assertEquals(0, pieceA.position.x);
        assertEquals(1, pieceA.position.y);
        assertEquals(pieceA, board.getPiece(0, 1).get());

        assertTrue(board.move(pieceA, 0, 0).moved);
        assertEquals(0, pieceA.position.x);
        assertEquals(0, pieceA.position.y);

        assertFalse(board.move(pieceA, 1, 0).moved, "Cannot move to occupied spots");
        assertFalse(board.move(pieceA, 1, 1).moved, "Cannot move diagonally");
        assertFalse(board.move(pieceA, 6, 6).moved, "Cannot move randomly");

        board.move(pieceA, 0, 1);
        board.move(pieceA, 1, 1);
        board.move(pieceA, 1, 2);
        board.move(pieceA, 2, 2);
        assertTrue(board.move(pieceA, 2, 3).moved);
        assertFalse(board.move(pieceA, 2, 4).moved, "Cannot move onto lake");
    }

    @Test
    void testScoutMovement() {
        Piece scout = board.A.findAnyByRank(Piece.SCOUT);
        board.place(scout, 0, 0);

        assertTrue(board.move(scout, 0, 1).moved);
        assertTrue(board.move(scout, 0, 4).moved);
        assertTrue(board.move(scout, 0, 7).moved);
        assertTrue(board.move(scout, 7, 7).moved);
        assertFalse(board.move(scout, 6, 6).moved); // sanity check

        assertTrue(board.move(scout, 6, 7).moved);
        assertFalse(board.move(scout, 6, 5).moved);
        assertTrue(board.move(scout, 6, 6).moved); // park next to lake
    }
    
    @Test
    void testIsOccupiedByOpposite() {
    	 Piece scout = board.A.findAnyByRank(Piece.SCOUT);
    	 assertTrue(board.place(scout, 0, 0));
         
         Piece marchal = board.B.findAnyByRank(1);
         assertTrue(board.place(marchal, 0, 9));
     	 
         assertTrue(board.isOccupiedByOpposite(scout.side, 0, 9));
         assertTrue(board.isOccupiedByOpposite(marchal.side, 0, 0));
    }
    
    @Test 
    void testAttack() {
    	Piece scout = board.A.findAnyByRank(Piece.SCOUT);
    	board.place(scout, 0, 0);
    	
    	
    	Piece marshal = board.B.findAnyByRank(10);
    	assertTrue(board.place(marshal, 0, 9));
    	
    	assertTrue(board.move(scout, 0, 1).moved);
    	MovementResult res = board.move(scout, 0, 9);
    	assertTrue(res.moved);
    	assertTrue(res.engagement == Engagement.LOSE);
    	assertFalse(scout.alive);
    	assertTrue(marshal.alive);
    	
    	Piece spy = board.A.findAnyByRank(Piece.SPY);
    	assertTrue(board.place(spy, 0, 0));
    	
    	// Move to marshal
    	for (int i = 1; i <= 8; i++) {
    		assertTrue(board.move(spy, 0, i).moved);   		
    	}
    	
    	res = board.move(spy, 0, 9);
    	assertTrue(res.moved);
    	assertTrue(res.engagement == Engagement.WIN);
    	assertFalse(marshal.alive);
    	assertTrue(spy.alive);
    	
    }
}
