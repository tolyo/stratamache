import { initGrid } from "../game/board.js";

export class BoardController {
  static $inject = ["$scope"];
  constructor($scope) {
    this.test = "hello";
    initGrid();
  }
}
