class PlaceholderView extends Backbone.Marionette.View {
  render() {
    const { chartType, title = "" } = this.options.spec;
    this.$el.addClass("chart-item") // ← chart‐level class
      .html(`
        <h4 class="chart-title">${title}</h4>
        <div class="placeholder-body">
          <p>TODO: implement <strong>${chartType}</strong> chart here</p>
        </div>
      `);
    return this;
  }
}

class GraphGridView extends Backbone.Marionette.View {
  initialize(options) {
    this.config = options.config;
  }

  render() {
    const { name, charts, columns = 3 } = this.config;
    const cols = Number(columns) || 3;

    this.clearPane();
    this.renderTitle(name);

    const container = this.createContainer();
    const grid = this.createGrid(container);
    const columnElements = this.createColumns(grid, cols);

    this.populateCharts(charts, columnElements);

    return this;
  }

  // Helpers

  clearPane() {
    this.$el.empty().addClass("graph-tab-view");
  }

  renderTitle(title) {
    this.$el.append(`<h3 class="pane__title">${title}</h3>`);
  }

  createContainer() {
    const container = document.createElement("div");
    container.className = "grid-container";
    this.el.appendChild(container);
    return container;
  }

  createGrid(container) {
    const grid = document.createElement("div");
    grid.className = "widgets-grid";
    container.appendChild(grid);
    return grid;
  }

  createColumns(grid, count) {
    const cols = [];
    for (let i = 0; i < count; i++) {
      const col = document.createElement("div");
      col.className = "widgets-grid__col";
      grid.appendChild(col);
      cols.push(col);
    }
    return cols;
  }

  populateCharts(charts, colEls) {
    charts.forEach((spec, idx) => {
      const widget = this.createWidgetFor(spec);
      // this is to ensure that the graphs will be ordered by rows
      colEls[idx % colEls.length].appendChild(widget);
    });
  }

  createWidgetFor(spec) {
    // instantiate the correct view
    const view =
      spec.chartType === "pie"
        ? new PieChartView({ model: new Backbone.Model(spec) })
        : new PlaceholderView({ spec });
    view.render();

    // wrap in Allure widget shell for correct styling
    const widget = document.createElement("div");
    widget.className = "widget island";
    widget.setAttribute("data-id", `${spec.chartType}-chart`);

    const body = document.createElement("div");
    body.className = "widget__body";
    body.appendChild(view.el);
    widget.appendChild(body);

    return widget;
  }
}
