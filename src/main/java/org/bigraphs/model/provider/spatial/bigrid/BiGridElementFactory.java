package org.bigraphs.model.provider.spatial.bigrid;

import lombok.Getter;
import lombok.Setter;
import org.bigraphs.framework.core.BigraphBuilder;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider;

import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.util.Map;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;


public class BiGridElementFactory {
    DefaultDynamicSignature signature;
    @Setter
    @Getter
    boolean withSite = true;

    private final Map<Integer, String> methodMap;

    public static BiGridElementFactory create() {
        return new BiGridElementFactory(BiSpaceSignatureProvider.getInstance().getSignature());
    }

    private BiGridElementFactory(DefaultDynamicSignature signature) {
        this.signature = signature;
        // Populate the map with TILE types and corresponding method names
        this.methodMap = Map.of(
                BiGridConfig.TILE_BLANK, "localeBlank",
                BiGridConfig.TILE_LOCALE_N, "localeSingleRouteNorth",
                BiGridConfig.TILE_LOCALE_E, "localeSingleRouteEast",
                BiGridConfig.TILE_LOCALE_S, "localeSingleRouteSouth",
                BiGridConfig.TILE_LOCALE_W, "localeSingleRouteWest",
                BiGridConfig.TILE_LOCALE_NESW, "crossingFour"
        );
        // Validate method existence at initialization - throw exception otherwise
        assertMethodsExist();
    }

    public PureBigraph createElement(int type, float x, float y, float stepSize) throws Exception {
        String methodName = methodMap.get(type);

        if (methodName == null) {
            throw new IllegalArgumentException("Unknown tile type: " + type);
        }

        // Get the method by name, assuming it takes two int parameters
        Method method = this.getClass().getMethod(methodName, float.class, float.class, float.class);

        // Invoke the method dynamically
        return (PureBigraph) method.invoke(this, x, y, stepSize);
    }

    public PureBigraph localeSingleRouteNorth(float x, float y, float stepSize) throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        Point2D.Float self = new Point2D.Float(x, y);
        Point2D.Float north = new Point2D.Float(x - stepSize, y);
        String localeLinkName = BiGridSupport.formatParamControl(self);
        String linkNameNorth = BiGridSupport.formatParamControl(north);
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy locale = builder.createRoot()
                .addChild("Locale", localeLinkName).down();
        locale.addChild("Route", linkNameNorth);
        addSiteToLocale(locale);
        return builder.createBigraph();
    }

    public PureBigraph localeSingleRouteEast(float x, float y, float stepSize) throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        Point2D.Float self = new Point2D.Float(x, y);
        Point2D.Float east = new Point2D.Float(x, y + stepSize);
        String localeLinkName = BiGridSupport.formatParamControl(self);
        String linkNameEast = BiGridSupport.formatParamControl(east);
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy locale = builder.createRoot()
                .addChild("Locale", localeLinkName).down();
        locale.addChild("Route", linkNameEast);
        addSiteToLocale(locale);
        return builder.createBigraph();
    }

    public PureBigraph localeSingleRouteSouth(float x, float y, float stepSize) throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        Point2D.Float self = new Point2D.Float(x, y);
        Point2D.Float south = new Point2D.Float(x + stepSize, y);
        String localeLinkName = BiGridSupport.formatParamControl(self);
        String linkNameSouth = BiGridSupport.formatParamControl(south);
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy locale = builder.createRoot()
                .addChild("Locale", localeLinkName).down();
        locale.addChild("Route", linkNameSouth);
        addSiteToLocale(locale);
        return builder.createBigraph();
    }

    public PureBigraph localeSingleRouteWest(float x, float y, float stepSize) throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        Point2D.Float self = new Point2D.Float(x, y);
        Point2D.Float west = new Point2D.Float(x, y - stepSize);
        String localeLinkName = BiGridSupport.formatParamControl(self);
        String linkNameWest = BiGridSupport.formatParamControl(west);
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy locale = builder.createRoot()
                .addChild("Locale", localeLinkName).down();
        locale.addChild("Route", linkNameWest);
        addSiteToLocale(locale);
        return builder.createBigraph();
    }

    public PureBigraph localeBlank(float x, float y, float stepSize) throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        Point2D.Float self = new Point2D.Float(x, y);
        String localeLinkName = BiGridSupport.formatParamControl(self);
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy locale = builder.createRoot().addChild("Locale", localeLinkName);
        addSiteToLocale(locale);
        return builder.createBigraph();
    }

    public PureBigraph crossingFour(float x, float y, float stepSize) throws InvalidConnectionException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        Point2D.Float self = new Point2D.Float(x, y);

        Point2D.Float north = new Point2D.Float(x - stepSize, y);
        Point2D.Float east = new Point2D.Float(x, y + stepSize);
        Point2D.Float south = new Point2D.Float(x + stepSize, y);
        Point2D.Float west = new Point2D.Float(x, y - stepSize);

        String localeLinkName = BiGridSupport.formatParamControl(self);
        String linkNameNorth = BiGridSupport.formatParamControl(north);
        String linkNameEast = BiGridSupport.formatParamControl(east);
        String linkNameSouth = BiGridSupport.formatParamControl(south);
        String linkNameWest = BiGridSupport.formatParamControl(west);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy locale = builder.createRoot()
                .addChild("Locale", localeLinkName).down();
        locale
                .addChild("Route", linkNameNorth)
                .addChild("Route", linkNameEast)
                .addChild("Route", linkNameSouth)
                .addChild("Route", linkNameWest)
        ;
        addSiteToLocale(locale);
        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private void addSiteToLocale(BigraphBuilder.NodeHierarchy<DefaultDynamicSignature> localeHierarchy) {
        if (withSite) {
            localeHierarchy.addSite();
        }
    }

    private void assertMethodsExist() {
        Class<?> clazz = this.getClass();
        for (Map.Entry<Integer, String> entry : methodMap.entrySet()) {
            String methodName = entry.getValue();
            try {
                Method method = clazz.getMethod(methodName, float.class, float.class, float.class);
                if (!method.getReturnType().equals(PureBigraph.class)) {
                    throw new AssertionError("Method " + methodName + " must return PureBigraph");
                }
            } catch (NoSuchMethodException e) {
                throw new AssertionError("Method " + methodName + " not found in " + clazz.getName(), e);
            }
        }
    }
}
