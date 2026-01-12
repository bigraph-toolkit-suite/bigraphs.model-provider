package org.bigraphs.model.provider.test.tool;

import lombok.Getter;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.visualization.SwingGraphStreamer;
import org.bigraphs.model.provider.spatial.bigrid.BiGridElementFactory;
import org.bigraphs.model.provider.spatial.bigrid.ConvexShapeBuilder;
import org.bigraphs.model.provider.test.BigridCreationTest;
import org.bigraphs.testing.BigraphUnitTestSupport;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.swing_viewer.util.DefaultMouseManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.InteractiveElement;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

/**
 * This test provides a "bigrid term builder" or "path selector" via a simple GUI.
 *
 * @author Dominik Grzelak
 */
@Disabled
public class NavigationPathCreator implements BigraphUnitTestSupport {
    static final String DUMP_PATH = "src/test/resources/dump/navigation/";

    @Test
    public void test_node_selector() throws Exception {
        float stepSize = 0.2f;
        float padding = 0f;
        Point2D.Float originPoint = new Point2D.Float(0, 0);
        List<Point2D.Float> convexPoints = new LinkedList<>();
        convexPoints.add(originPoint);
        convexPoints.add(new Point2D.Float(0f, 1f));
        convexPoints.add(new Point2D.Float(1f, 1f));
        convexPoints.add(new Point2D.Float(1f, 0f));

        PureBigraph generated = ConvexShapeBuilder.generateSingleRoot(convexPoints, stepSize, padding, BiGridElementFactory.create());
        BigraphFileModelManagement.Store.exportAsInstanceModel(generated, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(generated, new FileOutputStream("src/test/resources/dump/generated.xmi"));
        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(generated)
                .renderSites(true)
                .renderRoots(false);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();

        CustomMouseManager cmm = new CustomMouseManager(generated);
        graphViewer.getDefaultView().setMouseManager(cmm);

        InputStream styleStream = BigridCreationTest.class.getResourceAsStream("/graphStreamStyleHighlight.css");
        String style = new BufferedReader(new InputStreamReader(styleStream))
                .lines()
                .collect(Collectors.joining("\n"));
        graphStreamer.getGraph().setAttribute("ui.stylesheet", style);
        while (graphViewer.getDefaultView() != null) {
            System.out.println(cmm.getSelectedNodes());
            Thread.sleep(5000);
        }
        System.out.println("done");
    }

    // Custom MouseManager to handle mouse clicks
    static class CustomMouseManager extends DefaultMouseManager {
        @Getter
        Set<NodeSelection> selectedNodes = new LinkedHashSet<>();
        PureBigraph bigraph;

        public CustomMouseManager(PureBigraph bigraph) {
            this.bigraph = bigraph;
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            GraphicElement element = view.findGraphicElementAt(EnumSet.of(InteractiveElement.NODE), event.getX(), event.getY());
            if (element == null) {
                System.out.println("No element clicked.");
                return;
            }

            // Holding down SHIFT key while clicking removes the node from the selection
            boolean remove = event.getModifiersEx() == SHIFT_DOWN_MASK;
            if (event.getButton() == MouseEvent.BUTTON1) {
                System.out.println("Clicked on: " + element.getId());
                String elementId = element.getId();

                Node node = graph.getNode(elementId);
                for (int i = 0; i < node.getDegree(); i++) {
                    // System.out.println("Node " + i + ": " + node.getEdge(i).getId());
                    Node node1 = node.getEdge(i).getNode1();
                    node1.setAttribute("ui.class", "highlight");
                    Optional<? extends BigraphEntity.NodeEntity<?>> first = bigraph.getAllPlaces().stream()
                            .filter(BigraphEntityType::isNode)
                            .map(x -> (BigraphEntity.NodeEntity<?>) x)
                            .filter(x -> x.getName().equals(elementId)).findFirst();
                    if (first.isPresent()) {
                        BigraphEntity.NodeEntity<?> nodeEntity = first.get();
                        System.out.println("Node: " + nodeEntity.getName());
                        System.out.println("Root Index: " + bigraph.getTopLevelRoot(nodeEntity));
                        if (nodeEntity.getControl().equals(bigraph.getSignature().getControlByName("Locale"))) {
                            System.out.println("is LOCALE!");
                            if (remove) {
                                node1.setAttribute("ui.class", "control");
                                selectedNodes.stream().filter(x -> x.nodeId().equals(elementId)).findFirst().ifPresent(selectedNodes::remove);
                            } else {
                                node1.setAttribute("ui.class", "highlight");
                                selectedNodes.add(new NodeSelection(elementId, bigraph.getTopLevelRoot(nodeEntity).getIndex()));
                            }
                            return;
                        }
                    }
                }

            }
        }


    }

    record NodeSelection(String nodeId, int rootIndex) {
    }
}
