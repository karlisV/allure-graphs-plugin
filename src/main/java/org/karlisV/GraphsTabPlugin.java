package org.karlisV;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Aggregator2;
import io.qameta.allure.Reader;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class GraphsTabPlugin implements Aggregator2, Reader {
    private static final String FOLDER         = "data";
    private static final String TABS_FILENAME  = "graph-tabs.json";

    @Override
    public void aggregate(Configuration configuration,
                          java.util.List<LaunchResults> launchesResults,
                          ReportStorage storage) {
        // We expect the filter to have written graph-tabs.json under results dir
        // Allure Reader will have picked it up as extra with key "tabs"
        for (LaunchResults results : launchesResults) {
            Optional<Map> tabs = results.getExtra("tabs");
            if (tabs.isPresent()) {
                storage.addDataJson(FOLDER + "/" + TABS_FILENAME, tabs.get());
            }
        }
    }

    @Override
    public void readResults(Configuration configuration,
                            ResultsVisitor visitor,
                            Path directory) {
        try {
            Path tabsPath = directory.resolve(TABS_FILENAME);
            if (Files.isRegularFile(tabsPath)) {
                ObjectMapper objectMapper = configuration.requireContext(JacksonContext.class).getValue();
                Map root = objectMapper.readValue(tabsPath.toFile(), Map.class);
                visitor.visitExtra("tabs", root);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read graph-tabs.json", e);
        }
    }
}
