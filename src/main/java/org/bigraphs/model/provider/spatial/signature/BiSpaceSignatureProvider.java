package org.bigraphs.model.provider.spatial.signature;

import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.model.provider.base.BAbstractSignatureProvider;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * The default signature for the CF-MAPF Use Case.
 * This represents the location syntax.
 *
 * @author Dominik Grzelak
 */
public class BiSpaceSignatureProvider extends BAbstractSignatureProvider<DynamicSignature> {

    public static final String LOCALE_TYPE = "Locale";
    public static final String ROUTE_TYPE = "Route";
    public static final String OCCUPIED_TYPE = "OccupiedBy";

    private static BiSpaceSignatureProvider instance;
    private static final Object lock = new Object();

    private BiSpaceSignatureProvider() {
    }

    public static BiSpaceSignatureProvider getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new BiSpaceSignatureProvider();
                }
            }
        }
        return instance;
    }

    @Override
    public DynamicSignature getSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add(LOCALE_TYPE, 1)
                .add(ROUTE_TYPE, 1)
                .add(OCCUPIED_TYPE, 0)
        ;
        return defaultBuilder.create();
    }
}
