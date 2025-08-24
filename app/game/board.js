import { GRID } from "./constants.js";
import { Army } from "./army.js";
import { Side } from "./piece.js";
import { Square } from "./square.js";

export class Board {
  static WIDTH = 10;
  static HEIGHT = 10;

  static LAKE_POSITIONS = [
    [2, 4],
    [2, 5],
    [3, 4],
    [3, 5], // Left lake block
    [6, 4],
    [6, 5],
    [7, 4],
    [7, 5], // Right lake block
  ];

  constructor() {
    /**
     * @type {Square[][]}
     */
    this.grid = [];

    /**
     * @type {Army}
     */
    this.A;

    /**
     * @type {Army}
     */
    this.B;

    const board = document.getElementById("board");

    // Initialize grid with Square objects
    for (let x = 0; x < Board.WIDTH; x++) {
      this.grid[x] = [];
      const tileRow = document.createElement("div");
      tileRow.className = "board-row";
      board.appendChild(tileRow);
      for (let y = 0; y < Board.HEIGHT; y++) {
        const square = new Square(x, y);

        if ([0, 1, 3, 4].includes(y)) {
          square.startable = true;
          square.side = Side.A;
        } else if ([6, 7, 8, 9].includes(y)) {
          square.startable = true;
          square.side = Side.B;
        }

        this.grid[x][y] = square;

        const tile = document.createElement("div");
        tile.className = "tile";

        if (Board.isLakeTile(y, x)) {
          tile.classList.add("lake");
        }

        tile.id = `tile-${y}-${x}`;
        tileRow.appendChild(tile);

        square.elem = tile;
      }
    }

    // Mark lake positions as impassable
    for (const [x, y] of Board.LAKE_POSITIONS) {
      if (this.isInBounds(x, y)) {
        this.grid[x][y].passable = false;
      }
    }

    this.A = new Army(Side.A);
    this.B = new Army(Side.B);
  }

  /**
   * @param {number} x
   * @param {number} y
   * @returns {boolean}
   */
  static isLakeTile(x, y) {
    return Board.LAKE_POSITIONS.some((pos) => pos[0] === x && pos[1] === y);
  }

  /**
   * @param {Piece} piece
   * @returns {Square[]}
   */
  validMoves(piece) {
    const { x, y } = piece.position;
    const currentSquare = this.grid[x][y];
    const validSquares = [];

    const directions = [
      [-1, 0], // Up
      [1, 0], // Down
      [0, -1], // Left
      [0, 1], // Right
    ];

    for (const [dx, dy] of directions) {
      let newX = currentSquare.x + dx;
      let newY = currentSquare.y + dy;

      if (piece.rank === Piece.SCOUT) {
        while (this.isInBounds(newX, newY) && this.isPassable(newX, newY)) {
          if (this.isOccupied(piece.side, newX, newY)) break;

          validSquares.push(this.grid[newX][newY]);

          if (this.isOccupiedByOpposite(piece.side, newX, newY)) break;

          newX += dx;
          newY += dy;
        }
      } else {
        if (
          this.isInBounds(newX, newY) &&
          this.isPassable(newX, newY) &&
          (!this.isOccupied(piece.side, newX, newY) ||
            this.isOccupiedByOpposite(piece.side, newX, newY))
        ) {
          validSquares.push(this.grid[newX][newY]);
        }
      }
    }

    return validSquares;
  }

  /**
   * @param {Piece} piece
   * @param {number} x
   * @param {number} y
   * @returns {boolean}
   */
  isValidMove(piece, x, y) {
    return this.validMoves(piece).some((s) => s.x === x && s.y === y);
  }

  /**
   * @param {number} x
   * @param {number} y
   * @returns {boolean}
   */
  isPassable(x, y) {
    return this.grid[x][y].passable;
  }

  /**
   * @param {number} x
   * @param {number} y
   * @returns {Piece|null}
   */
  getPiece(x, y) {
    return this.grid[x][y].piece ?? null;
  }

  /**
   * @param {Side} side
   * @param {number} x
   * @param {number} y
   * @returns {boolean}
   */
  isOccupied(side, x, y) {
    const piece = this.getPiece(x, y);
    return piece !== null && piece.side === side;
  }

  /**
   * @param {Side} side
   * @param {number} x
   * @param {number} y
   * @returns {boolean}
   */
  isOccupiedByOpposite(side, x, y) {
    const piece = this.getPiece(x, y);
    return piece !== null && piece.side === Side.getOpposite(side);
  }

  /**
   * @param {Piece} piece
   * @param {number} x
   * @param {number} y
   * @returns {boolean}
   */
  place(piece, x, y) {
    if (
      !this.isInBounds(x, y) ||
      (piece.position && piece.position.x === x && piece.position.y === y) ||
      this.grid[x][y].side !== piece.side ||
      this.grid[x][y].piece !== null
    ) {
      return false;
    }

    if (piece.position) {
      this.grid[piece.position.x][piece.position.y].piece = null;
    }

    this.grid[x][y].piece = piece;
    piece.position = new Position(x, y);
    return true;
  }

  /**
   * @param {Piece} piece
   * @param {number} x
   * @param {number} y
   * @returns {MovementResult}
   */
  move(piece, x, y) {
    const res = new MovementResult();

    if (
      !piece.movable() ||
      !this.isInBounds(x, y) ||
      !this.isValidMove(piece, x, y)
    ) {
      res.moved = false;
      return res;
    }

    const destinationPiece = this.grid[x][y].piece;

    if (this.isOccupiedByOpposite(piece.side, x, y)) {
      const enemy = destinationPiece;
      const engagement = piece.attack(enemy);

      switch (engagement) {
        case Engagement.WIN:
          this.killPiece(enemy);
          this.movePiece(piece, x, y);
          break;
        case Engagement.LOSE:
          this.killPiece(piece);
          break;
        case Engagement.DRAW:
          this.killPiece(piece);
          this.killPiece(enemy);
          break;
      }

      res.engagement = engagement;
    } else {
      // Simple move
      this.grid[piece.position.x][piece.position.y].piece = null;
      piece.position.update(x, y);
      this.grid[x][y].piece = piece;
    }

    return res;
  }

  /**
   * @param {Piece} piece
   */
  killPiece(piece) {
    if (!piece || !piece.position) return;
    piece.alive = false;
    this.grid[piece.position.x][piece.position.y].piece = null;
    piece.position = null;
  }

  /**
   * @param {Piece} piece
   * @param {number} x
   * @param {number} y
   */
  movePiece(piece, x, y) {
    this.grid[piece.position.x][piece.position.y].piece = null;
    piece.position.update(x, y);
    this.grid[x][y].piece = piece;
  }

  /**
   * @param {number} x
   * @param {number} y
   * @returns {boolean}
   */
  isInBounds(x, y) {
    return x >= 0 && x < Board.WIDTH && y >= 0 && y < Board.HEIGHT;
  }
}

/**
 * @function addTilesToBoard
 * @param {HTMLElement} elem
 * @param {string} boardname
 * @return
 */
export function addTilesToBoard(elem, boardname) {
  GRID.forEach((y) => {
    // create row
    const tileRow = document.createElement("div");
    tileRow.className = "board-row";
    elem.appendChild(tileRow);

    // create tiles
    GRID.forEach((x) => {
      const tile = document.createElement("div");
      tile.className = `${boardname}-tile`;
      tile.id = `${boardname}-${y}-${x}`;
      tile.dataset.row = y.toString();
      tile.dataset.column = x.toString();
      tileRow.appendChild(tile);
    });
  });
}

export function initGrid() {
  const army = new Army(Side.NEITHER);
  army.shuffle();

  army.pieces.forEach((piece, index) => {
    const pieceDiv = document.createElement("div");
    pieceDiv.className = "piece";
    pieceDiv.id = `piece-${piece.rank}`;
    pieceDiv.innerText = piece.rank;
    board.appendChild(pieceDiv);
    const el = elements[index];

    requestAnimationFrame(() => {
      const rect = el.getBoundingClientRect();
      const boardRect = board.getBoundingClientRect();
      pieceDiv.style.position = "absolute";

      const left = Math.round(rect.left - boardRect.left - 2); // 2 to account for border
      const top = Math.round(rect.top - boardRect.top - 2); //

      pieceDiv.style.left = `${left}px`;
      pieceDiv.style.top = `${top}px`;
    });
  });
}
