"use strict";
const BASE_PATH = "/data";
const TAB_PATTERN = /graph-tab-([\w-]+)\.json/g;

async function fetchText(path) {
  const res = await fetch(path);
  if (!res.ok) throw new Error(`Failed to fetch ${path}: ${res.status}`);
  return res.text();
}

async function fetchTabConfig(tabName) {
  const url = `${BASE_PATH}/graph-tab-${tabName}.json`;
  const res = await fetch(url);
  if (!res.ok) throw new Error(`Failed to fetch ${url}: ${res.status}`);
  return res.json();
}

function extractTabNames(html) {
  const names = new Set();
  for (const [, name] of html.matchAll(TAB_PATTERN)) {
    names.add(name);
  }
  return Array.from(names);
}

function titleCase(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

function registerTab(config) {
  const { name, iconClass } = config;
  allure.api.addTab(name, {
    title: name,
    icon: `fa ${iconClass}`,
    route: name,
    onEnter: () => {
      return new GraphLayout(config);
    },
  });
}

function restartRouter() {
  Backbone.history.stop();
  Backbone.history.start();
}

async function initTabs() {
  try {
    const html = await fetchText(`${BASE_PATH}`);
    // TODO: add ordering numbers in file names as a requirement
    const tabNames = extractTabNames(html);

    for (const rawName of tabNames) {
      try {
        const cfg = await fetchTabConfig(rawName);
        const fullConfig = {
          name: titleCase(rawName),
          ...cfg,
        };
        registerTab(fullConfig);
      } catch (err) {
        console.error(`Failed to load tab "${rawName}":`, err);
      }
    }

    restartRouter();
  } catch (err) {
    console.error("Could not initialize tabs:", err);
  }
}

initTabs();
