"use strict";

class GraphLayout extends allure.components.AppLayout {
  initialize(options) {
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
        const model = new Backbone.Model({
          data: this.data,
          title: this.tabName,
        });
        return new PieChartView({ model });
      }
    }
  }
}
