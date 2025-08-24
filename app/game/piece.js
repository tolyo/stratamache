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

    /**
     * @type {HTMLDivElement}
     */
    this.element;

    // offsets for drag
    this.shiftX = 0;
    this.shiftY = 0;
    this.currentX = 0;
    this.currentY = 0;
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

  /**
   * @param {HTMLDivElement} el 
   */
  attach(el) {
    this.element = el;
    this.element.onmousedown = (e) => this.onmousedown(e);
    this.element.ondragstart = () => false;
    this.element.onmouseup = () => false;
  }

  onmousedown(e) {
    if (e.button !== undefined && e.button !== 0) return; // only left click or touch
    e.preventDefault();

    const box = this.element.getBoundingClientRect();
    this.shiftX = e.pageX - (box.left + window.scrollX);
    this.shiftY = e.pageY - (box.top + window.scrollY);

    this.element.style.position = "absolute";
    this.element.style.zIndex = 1000;
    this.element.classList.add("dragged");

    document.body.appendChild(this.element); // bring to top layer

    document.onmousemove = (moveEvt) => this.onmousemove(moveEvt);
    document.onmouseup = () => this.onmouseup();
  }

  onmousemove(e) {
    e.preventDefault();
    this.currentX = e.pageX - this.shiftX;
    this.currentY = e.pageY - this.shiftY;

    this.element.style.left = `${this.currentX}px`;
    this.element.style.top = `${this.currentY}px`;
  }

  onmouseup() {
    // Remove event listeners
    document.onmousemove = null;
    document.onmouseup = null;
    this.element.classList.remove("dragged");
    this.element.style.zIndex = 1;

    // Snap to nearest square if board and squares exist
    if (this.position && this.board) {
      const square = this.board.squares[this.position.y * 8 + this.position.x];
      if (square && square.elem) {
        const rect = square.elem.getBoundingClientRect();
        const boardRect = this.board.elem.getBoundingClientRect();

        this.element.style.left = `${rect.left - boardRect.left}px`;
        this.element.style.top = `${rect.top - boardRect.top}px`;
      }
    }
  }

  /**
   * Update piece position in logical board coordinates
   * @param {Position} pos
   */
  setPosition(pos) {
    this.position = pos;
  }
}
