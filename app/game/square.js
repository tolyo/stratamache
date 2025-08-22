export class Square {
  /**
   *
   * @param {number} x
   * @param {number} y
   */
  constructor(x, y) {
    this.x = x;
    this.y = y;
    this.passable = true;
    this.startable = false;
    this.side = null; // Side.A or Side.B
    this.piece = null; // Piece or null
  }
}
