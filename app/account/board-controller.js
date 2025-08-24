import { Army } from "../game/army.js";
import { Board } from "../game/board.js";
import { Side } from "../game/piece.js";

export class BoardController {
  static $inject = ["$scope"];
  constructor($scope) {
    this.board = new Board();

    const army = new Army(Side.NEITHER);
    army.shuffle();
    army.pieces.forEach((piece, index) => {
      const pieceDiv = document.createElement("div");
      pieceDiv.className = "piece";
      pieceDiv.id = `piece-${piece.rank}`;
      pieceDiv.innerText = piece.rank;
      board.appendChild(pieceDiv);
      const el = this.board[60 + index];

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
}
