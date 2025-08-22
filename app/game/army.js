import { Piece, Side } from "./piece.js";

export class Army {
  /**
   * @type {Piece[]}
   */
  pieces;

  /**
   * @type {Side}
   */
  side;

  /**
   * @param {Side} side
   */
  constructor(side) {
    this.side = side;
    this.pieces = [];

    this.pieces.push(new Piece(10)); // marshal
    this.pieces.push(new Piece(9)); // general
    this.pieces.push(...this.makePieces(8, 2)); // colonel
    this.pieces.push(...this.makePieces(7, 3)); // major
    this.pieces.push(...this.makePieces(6, 4)); // captain
    this.pieces.push(...this.makePieces(5, 4)); // lieutenant
    this.pieces.push(...this.makePieces(4, 4)); // sergeant
    this.pieces.push(...this.makePieces(3, 5)); // miner
    this.pieces.push(...this.makePieces(2, 8)); // scout
    this.pieces.push(new Piece(1)); // spy
    this.pieces.push(...this.makePieces(0, 6)); // bomb
    this.pieces.push(new Piece(-1)); // flag

    // Assign side to each piece
    this.pieces.forEach((piece) => {
      piece.side = this.side;
    });
  }

  /**
   * Creates an array of pieces with a given rank and count
   * @param {number} rank
   * @param {number} count
   * @returns {Piece[]}
   */
  makePieces(rank, count) {
    return Array.from({ length: count }, () => new Piece(rank));
  }

  /**
   * Find any piece by rank
   * @param {number} rank
   * @returns {Piece}
   * @throws {Error} if no piece with the rank is found
   */
  findAnyByRank(rank) {
    const found = this.pieces.find((p) => p.rank === rank);
    if (!found) {
      throw new Error(`No piece with rank ${rank} found`);
    }
    return found;
  }

  /**
   * Shuffle the pieces array in place using Fisher-Yates algorithm
   */
  shuffle() {
    for (let i = this.pieces.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [this.pieces[i], this.pieces[j]] = [this.pieces[j], this.pieces[i]];
    }
  }
}
