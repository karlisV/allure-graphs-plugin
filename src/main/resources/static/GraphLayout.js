"use strict";

class GraphLayout extends allure.components.AppLayout {
  initialize(options) {
    console.log(options);
    this.tabName = options.name;
    this.url = `/data/graph-tab-${this.tabName}.json`;
    this.data = options.data;
    this.chartType = options.chartType;
  }
  getContentView() {
    switch (this.chartType) {
      case "trend": {
        return "TODO TRENDS";
      }
      case "pie":
      default: {
        return "TODO PIE CHART";
      }
    }
  }
}
