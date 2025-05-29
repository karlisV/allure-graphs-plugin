class GraphLayout extends allure.components.AppLayout {
  initialize(config) {
    this.config = config;
  }

  getContentView() {
    return new GraphGridView({ config: this.config });
  }
}
