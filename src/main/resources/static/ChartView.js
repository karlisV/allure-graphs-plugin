// static/ChartView.js
"use strict";

class ChartView extends Backbone.Marionette.View {
  async render() {
    const spec = this.model.toJSON();
    const { chartType, data, title, options = {} } = spec;

    if (!data || typeof data !== "object") {
      this.$el.html(
        `<p class="error">Invalid or missing \`data\` for chart "${title}"</p>`
      );
      return this;
    }

    this.$el.addClass("chart-item").html(`
        <h4 class="chart-title">${title}</h4>
        <div class="chart-container"><canvas></canvas></div>
      `);

    if (typeof Chart === "undefined") {
      await new Promise((res, rej) => {
        const s = document.createElement("script");
        s.src = "https://cdn.jsdelivr.net/npm/chart.js";
        s.onload = res;
        s.onerror = rej;
        document.head.appendChild(s);
      });
    }

    const ctx = this.el.querySelector("canvas").getContext("2d");
    new Chart(ctx, {
      type: chartType,
      data,
      options: {
        responsive: true,
        plugins: { title: { display: false, text: title } },
        ...options,
      },
    });

    return this;
  }
}
