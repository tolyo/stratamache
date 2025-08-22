import { initGrid } from "../game/board.js";

export class BoardController {
  constructor($scope) {
    this.test = "hello";
    initGrid();
  }
}
