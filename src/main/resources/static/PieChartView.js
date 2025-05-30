class PieChartView extends Backbone.Marionette.View {
  async render() {
    const { data, title } = this.model.toJSON();

    this.$el.addClass("chart-item").html(
      `<div class="chart-container">
           <h4 class="chart-title">${title}</h4>
           <canvas></canvas>
         </div>`
    );

    // add chart.js lib
    if (typeof Chart === "undefined") {
      await new Promise((resolve, reject) => {
        const s = document.createElement("script");
        s.src = "https://cdn.jsdelivr.net/npm/chart.js";
        s.onload = resolve;
        s.onerror = reject;
        document.head.appendChild(s);
      });
    }

    // data prep
    const labels = Object.keys(data);
    const values = Object.values(data);

    // setup of the chart element
    const ctx = this.el.querySelector("canvas").getContext("2d");

    new Chart(ctx, {
      type: "pie",
      data: {
        labels,
        datasets: [
          {
            data: values,
            backgroundColor: labels.map(
              (_, i) => `hsl(${((i * 360) / labels.length) | 0},70%,70%)`
            ),
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: { position: "bottom" },
        },
      },
    });

    return this;
  }
}
