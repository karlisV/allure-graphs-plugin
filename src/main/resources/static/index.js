"use strict";

function registerSyncTabs() {
  try {
    // Synchronous XHR to avoid restarting router
    const xhr = new XMLHttpRequest();
    xhr.open("GET", "/data/graph-tabs.json", false);
    xhr.send(null);

    if (xhr.status === 200) {
      const root = JSON.parse(xhr.responseText);
      const pages = Array.isArray(root.pages) ? root.pages : [];

      pages.forEach((page) => {
        const key = page.key;
        const title = page.pageTitle
          ? page.pageTitle
          : key.charAt(0).toUpperCase() + key.slice(1);
        const icon = `fa ${page.iconClass}`;
        const columns = Number(page.columns) || 3;
        const charts = page.charts || [];

        allure.api.addTab(key, {
          title,
          icon,
          route: key,
          onEnter: () => new GraphLayout({ name: title, charts, columns }),
        });
      });
    }
  } catch (err) {
    console.error("Failed to register graph-tabs:", err);
  }
}

registerSyncTabs();
