# Allure Graph Plugin

## Overview

The Allure Graph Plugin enriches your Allure reports by allowing the inclusion of various graph types. By configuring the `graph-tab-{your-defined-key}.json` file, users can tailor their reports with detailed graphical data visualizations, enhancing the analysis and understanding of test outcomes.

## Configuration

### Key Configuration

- **File**: `graph-tab-performance.json`
- **Key Identification**: The plugin uses the text label, such as the “performance” in the example, to determine the unique “key” for each graph configuration page.
- **Multiple pages**: Create multiple pages in the report by specifying different keys for example `graph-tab-performance.json` and `graph-tab-analytics.json`

### JSON Configurations

- **Translations**: Specify the tab name under which the graph will be displayed in the Allure report.
- **Icon**: Assign an icon to represent the graph tab visually.
- **Chart Types**: Define the type of graph you wish to include. Each chart type is a JSON object that contains various properties and the data you wish to visualize.

## Supported Chart Types

The plugin supports a versatile range of chart types, allowing users to select the most appropriate visual representation for their data:

- **Pie Chart (key: `pie_chart`)**: Displays data in a circular chart, which is divided into sectors to illustrate numerical proportion.
- **Bar Chart (key: `bar_chart`)**: Available in horizontal and vertical orientations, bar charts are used to compare different categories of data.
- **Trend Chart (key: `trend`)**: A type of bar chart that includes historical data points to depict trends over time.
- **Stacked Bar Chart (key: `stacked_bar`)**: Similar to regular bar charts but with the ability to stack data on top of each other for comparative purposes.
- **Area Chart (key: `area_chart`)**: Depicts quantities through filled areas under lines, useful for representing cumulated totals using numbers or dates.
- **Gauge Chart (key: `gauge`)**: Visualizes data in a dial or speedometer format, ideal for showing performance metrics against predetermined thresholds.

## Getting Started

To integrate and use the Allure Graph Plugin in your project, follow these steps:

1. Use `mvn clean package` will create a zip file with all the necessary files you need to add to the Allure reporter
2. Include the plugin into your Allure report generation setup.
3. When creating the results be sure to generate `graph-tab-{some-key}.json` file, the `{some-key}` part is used to generate the page name so it must be unique
4. Customize the `graph-tab-{some-key}.json` file to define the graphs you need, based on the available chart types and their configurations.

For more specific setup instructions, usage examples, and contribution guidelines, refer to the subsequent sections.

## Contribution

Contributions are welcome! If you're interested in improving the Allure Graph Plugin or have suggestions, please refer to our contribution guidelines for more information on how to get involved.
