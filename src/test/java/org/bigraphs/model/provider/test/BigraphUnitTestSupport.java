package org.bigraphs.model.provider.test;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.bigraphs.framework.visualization.SwingGraphStreamer;
import org.graphstream.ui.view.Viewer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface BigraphUnitTestSupport {

    default Viewer GUI(PureBigraph bigraph, boolean renderSites, boolean renderRoots) {
        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(bigraph)
                .renderSites(renderSites)
                .renderRoots(renderRoots);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();

//        InputStream styleStream = BigraphUnitTestSupport.class.getResourceAsStream("/light-style.css");
        InputStream styleStream = SwingGraphStreamer.class.getResourceAsStream("/graphStreamStyleHighlight.css");
        String style = new BufferedReader(new InputStreamReader(styleStream))
                .lines()
                .collect(Collectors.joining("\n"));
        graphStreamer.getGraph().setAttribute("ui.stylesheet", style);
        return graphViewer;
    }

    default void eb(Bigraph<?> bigraph, String name, String basePath) {
        eb(bigraph, name, basePath, true);
    }

    default void eb(Bigraph<?> bigraph, String name, String basePath, boolean asTree) {
        try {
            BigraphGraphvizExporter.toPNG(bigraph, asTree, new File(basePath + name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    default void print(PureBigraph bigraph) {
        try {
            BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    default void printMetaModel(PureBigraph bigraph) {
        try {
            BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    default Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }

    default void writeToFile(String content, String filePath) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
