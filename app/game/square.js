export class Square {
  /**
   * @param {number} x
   * @param {number} y
   */
  constructor(x, y) {
    /** @type {number} */
    this.x = x;
    /** @type {number} */
    this.y = y;

    /** @type {boolean} */
    this.passable = true;

    /** @type {boolean} */
    this.startable = false;

    /** @type {import("./piece.js").Side} */
    this.side = null; // Side.A or Side.B

    /** @type {import("./piece.js").Piece} */
    this.piece = null;

    /** @type {HTMLDivElement} */
    this.elem = null;
  }
}
