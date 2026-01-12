package org.bigraphs.model.provider.spatial.signature;

import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * A signature provider for diagonal directional bi-spatial bigraphs.
 * Extends the directional signature with diagonal route types (8 directions total).
 *
 * Directions:
 * - 4 cardinal directions: Left, Right, Forward, Back
 * - 4 diagonal directions: ForwardLeft, ForwardRight, BackLeft, BackRight
 *
 * @author Tianxiong Zhang
 */
public class DiagonalDirectionalBiSpaceSignatureProvider extends DirectionalBiSpaceSignatureProvider {

    // Diagonal directions (4) - horizontal plane
    public static final String FORWARD_LEFT_ROUTE_TYPE = "ForwardLeftRoute";
    public static final String FORWARD_RIGHT_ROUTE_TYPE = "ForwardRightRoute";
    public static final String BACK_LEFT_ROUTE_TYPE = "BackLeftRoute";
    public static final String BACK_RIGHT_ROUTE_TYPE = "BackRightRoute";

    private static DiagonalDirectionalBiSpaceSignatureProvider instance;
    private static final Object lock = new Object();

    DiagonalDirectionalBiSpaceSignatureProvider() {
        super();
    }

    public static DiagonalDirectionalBiSpaceSignatureProvider getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DiagonalDirectionalBiSpaceSignatureProvider();
                }
            }
        }
        return instance;
    }

    @Override
    public DynamicSignature getSignature() {
        DynamicSignatureBuilder ext = pureSignatureBuilder();
        ext.add(FORWARD_LEFT_ROUTE_TYPE, 1)
           .add(FORWARD_RIGHT_ROUTE_TYPE, 1)
           .add(BACK_LEFT_ROUTE_TYPE, 1)
           .add(BACK_RIGHT_ROUTE_TYPE, 1);

        return BigraphUtil.composeSignatures(super.getSignature(), ext.create());
    }
}
