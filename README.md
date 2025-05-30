# Allure Graph Plugin

## Overview

The Allure Graph Plugin enriches your Allure reports by adding fully-configurable graph pages (tabs). You need to provide a `graph-tabs.json` file in your `allure-results/` directory. That file contains an array of pages, each defining a tab with its own icon, layout, and charts.

---

## Installation

1. **Build the plugin**

   ```bash
   mvn clean package
   ```

   This produces `target/allure-graphs-plugin.zip`.

2. **Install into Allure**
   This step depends on how Allure is installed on your system. Locate your Allure distribution directory and unzip the plugin into its `plugins/` folder. For example, if Allure is installed with `Homebrew` on mac, you would run this command for the default setup:

   ```bash
   unzip target/allure-graphs-plugin.zip -d /usr/local/opt/allure/libexec/plugins/allure-graphs-plugin/
   ```

   If you installed Allure via another package manager or have a custom path, adjust the destination directory so the plugin folder lives under `[ALLURE_HOME]/plugins/allure-graphs-plugin/`.

3. **Enable the plugin**
   After unzipping into the `plugins/` folder you must enable the plugin by adding it to your Allure configuration file. In `[ALLURE_HOME]/config/allure.yml`, include:

   ```yaml
   plugins:
     - other-plugins
     - allure-graphs-plugin
   ```
    If you use some other custom profile, enable the plugin in that config.
4. **Add your JSON config**
   Before the report generation (during or after the tests are finished) write a JSON file in the allure results folder:

   ```bash
   allure-results/graph-tabs.json
   ```

   The plugin will discover this file and build each tab defined under its `pages` array.

5. **Generate the report** **Generate the report**

   ```bash
   allure serve allure-results
   ```

---

## Configuration

### File naming

In your result generation logic you have to generate `graph-tabs.json` file in the `allure-results` folder. And when you run report generation command the reporter will pick up the file.

### JSON schema

```json
{
  "pages": [
    {
      "key": "performance",
      "pageTitle": "Performance Metrics"
      "iconClass": "fa-hashtag",
      "columns": 2,
      "charts": [
        {
          "chartType": "pie",
          "title": "Endpoint Hits",
          "data": {
            "/posts": 1,
            "/comments": 1
          }
        }
        // …add more chart definitions per page…
      ]
    }
    // …add more pages…
  ]
}
```

- **`pages`**: array of page definitions
- **`key`**: unique identifier; used as tab route and title 
- **`pageTitle`**: name of the page (tab)
- **`iconClass`**: Font-Awesome v4 icon (no prefix)
- **`columns`**: integer ≥1; charts per row (default: 3)
- **`charts`**: array of chart specs; unknown types show a placeholder

---

## Supported Chart Types

- **`pie`**: renders a pie chart using Chart.js
- **`trend`**: renders a simple trend (bar/line) chart
- **Others**: unrecognized types display a placeholder; you can extend to support bar, area, gauge, etc.

---

## Example JSON

```json
// allure-results/graph-tabs.json
{
  "pages": [
    {
      "key": "some-kpis",
      "pageTitle": "Some KPIs",
      "iconClass": "fa-hashtag",
      "charts": [
        {
          "chartType": "pie",
          "title": "Endpoint Hits",
          "data": {
            "/posts": 1,
            "/comments": 1
          }
        },
        {
          "chartType": "pie",
          "title": "Mock Chart 2",
          "data": {
            "/messages": 91,
            "/photos": 98,
            "/likes": 62,
            "/users": 94,
            "/shares": 18
          }
        }
        // …more chart objects…
      ]
    },
    {
      "key": "performance-metrics",
      "pageTitle": "Performance metrics",
      "iconClass": "fa-car",
      "columns": 1,
      "charts": [
        {
          "chartType": "trend",
          "title": "Average Response Time",
          "data": {
            "/posts": 1329.0,
            "/comments": 304.0
          }
        }
      ]
    }
  ]
}
```

---

---

## Contribution

Contributions welcome! Feel free to:

- Add new chart types (bar, area, gauge…)
- Improve placeholders or docs
- Fix bugs or update dependencies
- Add tests

Please fork, adhere to existing style and be sure to add docs with your PR.
