"use strict";
(async () => {
  const html = await (await fetch("/data")).text();
  const regex = /graph-tab-([\w-]+)\.json/g;
  const names = [];
  let match;

  while ((match = regex.exec(html))) {
    const name = match[1];
    if (!names.includes(name)) names.push(name);
  }

  for (const tabName of names) {
    try {
      const res = await fetch(`/data/graph-tab-${tabName}.json`);
      const body = await res.json();

      allure.api.addTab(tabName, {
        title: tabName[0].toUpperCase() + tabName.slice(1),
        icon: `fa ${body.iconClass}`,
        route: tabName,
        onEnter: () => "todo",
      });
    } catch (err) {
      console.error(`Failed to load tab "${tabName}":`, err);
    }
  }

  // Restart router so deep-links work
  Backbone.history.stop();
  Backbone.history.start();
})();
