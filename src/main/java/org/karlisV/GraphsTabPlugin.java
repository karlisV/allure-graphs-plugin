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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GraphsTabPlugin implements Aggregator2, Reader {

    private static final Pattern FILE_PATTERN = Pattern.compile("graph-tab-(.*)\\.json");
    private static final String FOLDER = "data";
    private final List<String> keys = new ArrayList<>();


    public GraphsTabPlugin() {}

    @Override
    public void aggregate(Configuration configuration, List<LaunchResults> launchesResults, ReportStorage storage) {
        for (LaunchResults results : launchesResults) {
            for(String key : keys){
               Optional<Map> graphData = results.getExtra(key);
               if(graphData.isPresent()){
                   String fileName = FOLDER + "/graph-tab-" + key + ".json";
                   storage.addDataJson(fileName, graphData.get());
               }
            }
        }
    }

    @Override
    public void readResults(Configuration configuration, ResultsVisitor visitor, Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        ObjectMapper objectMapper = context.getValue();

        try (Stream<Path> files = Files.walk(directory)) {
            files.filter(Files::isRegularFile)
                    .forEach(path -> {
                        String filename = path.getFileName().toString();
                        Matcher matcher = FILE_PATTERN.matcher(filename);
                        if (matcher.matches()) {
                            String identifier = matcher.group(1);
                            this.keys.add(identifier);
                            try {
                                String content = new String(Files.readAllBytes(path));
                                Map data = objectMapper.readValue(content, Map.class);
                                visitor.visitExtra(identifier, data);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read file " + path, e);
                            }
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to process results directory", e);
        }
    }
}
