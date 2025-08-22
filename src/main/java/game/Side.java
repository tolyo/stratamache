package game;

public enum Side {
	A, B, NEITHER;
	
	public static Side getOpposite(Side side) {
		return switch (side) {
			case A -> B;
			case B -> A;
			default -> throw new IllegalArgumentException("Unexpected value: " + side);
		};
	}
	
}

