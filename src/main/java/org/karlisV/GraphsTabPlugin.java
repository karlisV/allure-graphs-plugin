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
    private static final String FOLDER        = "data";
    private static final String TABS_FILENAME = "graph-tabs.json";

    @Override
    public void aggregate(Configuration configuration,
                          List<LaunchResults> launchesResults,
                          ReportStorage storage) {
        for (LaunchResults results : launchesResults) {
            Optional<Map> tabs = results.getExtra("tabs");
            tabs.ifPresent(m -> storage.addDataJson(FOLDER + "/" + TABS_FILENAME, m));
        }
    }

    @Override
    public void readResults(Configuration configuration,
                            ResultsVisitor visitor,
                            Path directory) {
        Path tabsPath = directory.resolve(TABS_FILENAME);
        if (!Files.isRegularFile(tabsPath)) {
            return;
        }
        Map<String, Object> root = loadJson(configuration, tabsPath);
        validateRoot(root);
        visitor.visitExtra("tabs", root);
    }

    // --- Helpers ---

    private Map<String, Object> loadJson(Configuration configuration, Path path) {
        ObjectMapper mapper = configuration.requireContext(JacksonContext.class).getValue();
        try {
            return mapper.readValue(path.toFile(), Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse " + TABS_FILENAME, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateRoot(Map<String, Object> root) {
        Object pagesObj = root.get("pages");
        if (!(pagesObj instanceof List)) {
            throw new RuntimeException("Invalid format: 'pages' must be an array");
        }
        List<Map<String, Object>> pages = (List<Map<String, Object>>) pagesObj;
        if (pages.isEmpty()) {
            throw new RuntimeException("'pages' array must contain at least one page definition");
        }
        pages.forEach(this::validatePage);
    }

    private void validatePage(Map<String, Object> page) {
        // we transform key if by mistake there's a space in it (we change spaces to "-")
        String rawKey = getRequiredString(page, "key", "page");
        String sanitizedKey = rawKey.trim().replaceAll("\\s+", "-");
        page.put("key", sanitizedKey);

        Object titleObj = page.get("pageTitle");
        if (titleObj != null && !(titleObj instanceof String)) {
            throw new RuntimeException("'pageTitle' must be a string in page '" + sanitizedKey + "'");
        }

        Object cols = page.get("columns");
        if (cols != null && !(cols instanceof Number)) {
            throw new RuntimeException("'columns' must be a number in page '" + sanitizedKey + "'");
        }

        Object chartsObj = page.get("charts");
        if (!(chartsObj instanceof List)) {
            throw new RuntimeException("Page '" + sanitizedKey + "' must contain a 'charts' array");
        }
        List<Map<String, Object>> charts = (List<Map<String, Object>>) chartsObj;
        if (charts.isEmpty()) {
            throw new RuntimeException("Page '" + sanitizedKey + "' must have at least one chart definition");
        }
        charts.forEach(chart -> validateChart(sanitizedKey, chart));
    }

    private void validateChart(String pageKey, Map<String, Object> chart) {
        String type = getRequiredString(chart, "chartType", "chart in page '" + pageKey + "'");
        getRequiredString(chart, "title", "chart of type '" + type + "' in page '" + pageKey + "'");
        if ("pie".equalsIgnoreCase(type)) {
            validatePieData(pageKey, chart);
        } else {
            // TODO: add validations for other chart types
        }
    }

    @SuppressWarnings("unchecked")
    private void validatePieData(String pageKey, Map<String, Object> chart) {
        Object dataObj = chart.get("data");
        if (!(dataObj instanceof Map)) {
            throw new RuntimeException("Pie chart in page '" + pageKey + "' must have a 'data' object");
        }
        Map<Object, Object> data = (Map<Object, Object>) dataObj;
        if (data.isEmpty()) {
            throw new RuntimeException("Pie chart in page '" + pageKey + "' must have at least one data point");
        }
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new RuntimeException("Data key must be a string in pie chart of page '" + pageKey + "'");
            }
            if (!(entry.getValue() instanceof Number)) {
                throw new RuntimeException("Data value must be numeric in pie chart of page '" + pageKey + "'");
            }
        }
    }

    private String getRequiredString(Map<String, Object> map, String field, String context) {
        Object val = map.get(field);
        if (!(val instanceof String) || ((String) val).trim().isEmpty()) {
            throw new RuntimeException("Missing or invalid '" + field + "' in " + context);
        }
        return (String) val;
    }
}
