package org.bigraphs.model.provider.spatial.bigrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bigraphs.model.provider.base.BLocationModelData;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

public class BLocationModelDataFactory {

    /**
     *
     * @param m rows
     * @param n cols
     * @param startX origin x
     * @param startY origin y
     * @param stepSizeX step size in x-direction
     * @param stepSizeY step size in y-direction
     * @return a data model representing the mxn grid
     */
    public static BLocationModelData createGrid(int m, int n, float startX, float startY, float stepSizeX, float stepSizeY) {
        BLocationModelData lmpd = new BLocationModelData();
        List<BLocationModelData.Locale> locales = new LinkedList<>();
        List<BLocationModelData.Route> routes = new LinkedList<>();

        // Create locales
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                float x = startX + i * stepSizeX;
                float y = startY + j * stepSizeY;
                String name = "v" + (i * n + j);
                locales.add(BLocationModelData.Locale.builder().name(name).center(new Point2D.Float(x, y)).build());
            }
        }

        // Create routes
        int routeIdCnt = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                int currentIndex = i * n + j;
                // Horizontal routes
                if (j < n - 1) {
                    int rightIndex = i * n + (j + 1);
                    routes.add(BLocationModelData.Route.builder()
                            .name("l" + routeIdCnt)
                            .startingPoint(locales.get(currentIndex).getCenter())
                            .endingPoint(locales.get(rightIndex).getCenter())
                            .build());
                    routeIdCnt++;
                }
                // Vertical routes
                if (i < m - 1) {
                    int downIndex = (i + 1) * n + j;
                    routes.add(BLocationModelData.Route.builder()
                            .name("l" + routeIdCnt)
                            .startingPoint(locales.get(currentIndex).getCenter())
                            .endingPoint(locales.get(downIndex).getCenter())
                            .build());
                    routeIdCnt++;
                }
            }
        }

        lmpd.getLocales().addAll(locales);
        lmpd.getRoutes().addAll(routes);

        return lmpd;
    }

    // JSON Serialization
    public static String toJson(BLocationModelData lmpd) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(lmpd);
    }

    // JSON Deserialization
    public static BLocationModelData fromJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, BLocationModelData.class);
    }
}
