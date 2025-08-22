package game;


public class Piece {
	
	public static final int BOMB = 0;
	public static final int FLAG = -1;
	public static final int SPY = 1;
	public static final int SCOUT = 2;

	public int rank;
	boolean revealed = false;
	boolean alive = true;
	Position position;
	Side side = Side.NEITHER;
	
	Piece(int rank) {
		this.rank = rank;
	}
	
	boolean movable() {
        	return !(this.rank == 0 |	 this.rank == -1);
	}
	
	Engagement attack(Piece target) {
	    return switch (this.rank) {
	        case BOMB, FLAG -> throw new RuntimeException("Piece cannot attack");
	        case 3 -> switch (target.rank) {
	            case BOMB -> Engagement.WIN;
	            default -> switch (Integer.compare(this.rank, target.rank)) {
	                case 1 -> Engagement.WIN;
	                case 0 -> Engagement.DRAW;
	                default -> Engagement.LOSE;
	            };
	        };
	        case SPY -> switch (target.rank) {
	        	case BOMB -> Engagement.LOSE;
	            case 10 -> Engagement.WIN;
	            default -> switch (Integer.compare(this.rank, target.rank)) {
	                case 1 -> Engagement.WIN;
	                case 0 -> Engagement.DRAW;
	                default -> Engagement.LOSE;
	            };
	        };
	        default -> switch (target.rank) {	        
	            case BOMB -> Engagement.LOSE;
	            default -> switch (Integer.compare(this.rank, target.rank)) {
	                case 1 -> Engagement.WIN;
	                case 0 -> Engagement.DRAW;
	                default -> Engagement.LOSE;
	            };
	        };
	    };
	}


	
}
