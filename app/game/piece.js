/**
 * @typedef {Object} Position
 * @property {number} x
 * @property {number} y
 */

/**
 * @enum {string}
 */
export const Side = {
  RED: "RED",
  BLUE: "BLUE",
  NEITHER: "NEITHER",
};

/**
 * @enum {string}
 */
export const Engagement = {
  WIN: "WIN",
  LOSE: "LOSE",
  DRAW: "DRAW",
};

/**
 * @readonly
 * @type {number}
 */
export const BOMB = 0;

/**
 * @readonly
 * @type {number}
 */
export const FLAG = -1;

/**
 * @readonly
 * @type {number}
 */
export const SPY = 1;

/**
 * @readonly
 * @type {number}
 */
export const SCOUT = 2;

export class Piece {
  /**
   * @param {number} rank
   */
  constructor(rank) {
    this.rank = rank;

    /**
     * @type {boolean}
     */
    this.revealed = false;

    /**
     * @type {boolean}
     */
    this.alive = true;

    /**
     * @type {Position|null}
     */
    this.position = null;

    /**
     * @type {Side}
     */
    this.side = Side.NEITHER;
  }

  /**
   * Check if the piece is movable.
   * @returns {boolean}
   */
  movable() {
    return this.rank !== BOMB && this.rank !== FLAG;
  }

  /**
   * Handle engagement between this piece and a target.
   * @param {Piece} target
   * @returns {Engagement}
   */
  attack(target) {
    switch (this.rank) {
      case BOMB:
      case FLAG:
        throw new Error("Piece cannot attack");

      case 3:
        switch (target.rank) {
          case BOMB:
            return Engagement.WIN;
          default:
            return this._compareRanks(target.rank);
        }

      case SPY:
        switch (target.rank) {
          case BOMB:
            return Engagement.LOSE;
          case 10:
            return Engagement.WIN;
          default:
            return this._compareRanks(target.rank);
        }

      default:
        switch (target.rank) {
          case BOMB:
            return Engagement.LOSE;
          default:
            return this._compareRanks(target.rank);
        }
    }
  }

  _compareRanks(targetRank) {
    const comparison = Math.sign(this.rank - targetRank);
    switch (comparison) {
      case 1:
        return Engagement.WIN;
      case 0:
        return Engagement.DRAW;
      default:
        return Engagement.LOSE;
    }
  }
}
