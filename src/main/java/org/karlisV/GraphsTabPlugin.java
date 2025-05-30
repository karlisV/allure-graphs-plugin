package org.karlisV;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Aggregator2;
import io.qameta.allure.Reader;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GraphsTabPlugin implements Aggregator2, Reader {
    private static final String DATA_FOLDER     = "data";
    private static final String TABS_JSON_FILE  = "graph-tabs.json";

    @Override
    public void aggregate(Configuration configuration,
                          List<LaunchResults> allLaunches,
                          ReportStorage storage) {
        for (LaunchResults launch : allLaunches) {
            launch.getExtra("tabs")
                  .ifPresent(tabsExtra -> 
                      storage.addDataJson(DATA_FOLDER + "/" + TABS_JSON_FILE, tabsExtra)
                  );
        }
    }

    @Override
    public void readResults(Configuration configuration,
                            ResultsVisitor visitor,
                            Path resultsDirectory) {
        Path tabsJsonPath = resultsDirectory.resolve(TABS_JSON_FILE);
        if (!Files.isRegularFile(tabsJsonPath)) {
            return;
        }
        Map<String,Object> tabsRoot = loadJson(configuration, tabsJsonPath);
        validateTabsRoot(tabsRoot);
        visitor.visitExtra("tabs", tabsRoot);
    }


    @SuppressWarnings("unchecked")
    private Map<String,Object> loadJson(Configuration configuration, Path jsonPath) {
        ObjectMapper mapper = configuration.requireContext(JacksonContext.class).getValue();
        try {
            return mapper.readValue(jsonPath.toFile(), Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse " + TABS_JSON_FILE, e);
        }
    }

    // top‐level validation 

    private void validateTabsRoot(Map<String,Object> tabsRoot) {
        Object pagesField = tabsRoot.get("pages");
        // Was `pagesRoot`, now correctly `tabsRoot`
        List<Map<String,Object>> pages = expectListOf(tabsRoot, "pages", pagesField);
        if (pages.isEmpty()) {
            throw new RuntimeException("'pages' array must contain at least one page");
        }
        for (Map<String,Object> pageMap : pages) {
            validatePageDefinition(pageMap);
        }
    }

    //  Per‐page validation

    private void validatePageDefinition(Map<String,Object> pageMap) {
        // Key is mandatory
        String rawKey = requireString(pageMap, "key", "page");
        String sanitizedKey = rawKey.trim().replaceAll("\\s+", "-");
        pageMap.put("key", sanitizedKey);

        // title is not mandatory- we use key if pageTitle not present
        Object titleOverride = pageMap.get("pageTitle");
        if (titleOverride != null && !(titleOverride instanceof String)) {
            throw new RuntimeException("'pageTitle' must be a string in page '" + sanitizedKey + "'");
        }

        // Columns we fallback to 3 if not present on the FE
        Object columnsValue = pageMap.get("columns");
        if (columnsValue != null && !(columnsValue instanceof Number)) {
            throw new RuntimeException("'columns' must be a number in page '" + sanitizedKey + "'");
        }

        // charts array check
        Object chartsField = pageMap.get("charts");
        List<Map<String,Object>> chartsList = expectListOf(pageMap, "charts", chartsField);
        if (chartsList.isEmpty()) {
            throw new RuntimeException("Page '" + sanitizedKey + "' must define at least one chart");
        }
        for (Map<String,Object> chartMap : chartsList) {
            validateChartDefinition(sanitizedKey, chartMap);
        }
    }

    // 
    private void validateChartDefinition(String pageKey, Map<String,Object> chartMap) {
        // chartType is mandatory
        String chartType = requireString(chartMap, "chartType", "chart in page '" + pageKey + "'");
        requireString(chartMap, "title", "chart '" + chartType + "' in page '" + pageKey + "'");

        // full Chart.js data map
        Object dataField = chartMap.get("data");
        Map<String,Object> chartData = expectMapOf(chartMap, "data", dataField);

        // datasets are mandatory
        if (chartData.containsKey("datasets")) {
            validateChartJsData(pageKey, chartType, chartData);
        } else {
            throw new RuntimeException(
                "Chart '" + chartType + "' in page '" + pageKey +
                "' must have a full Chart.js data object (labels & datasets)"
            );
        }
    }

    // ─── Chart.js data validation ────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void validateChartJsData(String pageKey, String chartType, Map<String,Object> chartData) {
        // datasets array (labels may be optional)
        Object datasetsField = chartData.get("datasets");
        List<?> datasetsList = expectListOf(chartData, "datasets", datasetsField);
        if (datasetsList.isEmpty()) {
            throw new RuntimeException("Chart '" + chartType + "' in page '"
                + pageKey + "' must have non-empty 'datasets'");
        }

        // chart can have multiple datasets we check each one
        for (Object datasetEntry : datasetsList) {
            if (!(datasetEntry instanceof Map)) {
                throw new RuntimeException("Each dataset in chart '" + chartType +
                    "' of page '" + pageKey + "' must be an object");
            }
            Map<String,Object> datasetMap = (Map<String,Object>) datasetEntry;

            Object dataPointsField = datasetMap.get("data");
            List<?> dataPoints = expectListOf(datasetMap, "data", dataPointsField);
            // data field is required in datasets
            if (dataPoints.isEmpty()) {
                throw new RuntimeException("Dataset in chart '" + chartType +
                    "' of page '" + pageKey + "' must have non-empty 'data'");
            }

            // usually keys are numbers but there are exceptions here we check the exceptions for specific chartType's
            switch (chartType.toLowerCase(Locale.ROOT)) {
                case "bubble":
                    validateBubbleDataPoints(pageKey, chartType, dataPoints);
                    break;
                case "scatter":
                    validateScatterDataPoints(pageKey, chartType, dataPoints);
                    break;
                default:
                    // default: all data entries must be Number
                    for (Object value : dataPoints) {
                        if (!(value instanceof Number)) {
                            throw new RuntimeException("Data value in chart '" + chartType +
                                "' of page '" + pageKey + "' must be numeric (found " + value + ")");
                        }
                    }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validateBubbleDataPoints(String pageKey, String chartType, List<?> points) {
        for (Object pointObj : points) {
            if (!(pointObj instanceof Map)) {
                throw new RuntimeException("Bubble chart '" + chartType +
                    "' in page '" + pageKey + "' data points must be objects");
            }
            Map<String,Object> pointMap = (Map<String,Object>) pointObj;
            for (String coordKey : Arrays.asList("x","y","r")) {
                Object coordValue = pointMap.get(coordKey);
                if (!(coordValue instanceof Number)) {
                    throw new RuntimeException("Bubble chart '" + chartType +
                        "' in page '" + pageKey + "' requires numeric '" + coordKey + "'");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validateScatterDataPoints(String pageKey, String chartType, List<?> points) {
        for (Object pointObj : points) {
            if (!(pointObj instanceof Map)) {
                throw new RuntimeException("Scatter chart '" + chartType +
                    "' in page '" + pageKey + "' data points must be objects");
            }
            Map<String,Object> pointMap = (Map<String,Object>) pointObj;
            for (String coordKey : Arrays.asList("x","y")) {
                Object coordValue = pointMap.get(coordKey);
                if (!(coordValue instanceof Number)) {
                    throw new RuntimeException("Scatter chart '" + chartType +
                        "' in page '" + pageKey + "' requires numeric '" + coordKey + "'");
                }
            }
        }
    }


    private String requireString(Map<String,Object> sourceMap, String fieldName, String context) {
        Object value = sourceMap.get(fieldName);
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw new RuntimeException("Missing or invalid '" + fieldName + "' in " + context);
        }
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> expectListOf(Map<String,Object> sourceMap, String fieldName, Object value) {
        if (!(value instanceof List)) {
            throw new RuntimeException("Field '" + fieldName + "' must be an array");
        }
        return (List<T>) value;
    }

    @SuppressWarnings("unchecked")
    private <K,V> Map<K,V> expectMapOf(Map<String,Object> sourceMap, String fieldName, Object value) {
        if (!(value instanceof Map)) {
            throw new RuntimeException("Field '" + fieldName + "' must be an object");
        }
        return (Map<K,V>) value;
    }
}
