package org.bigraphs.model.provider.spatial.signature;

import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * A signature provider for 3D bi-spatial bigraphs with 10-directional routes.
 * Extends the diagonal directional signature with vertical route types.
 * <p>
 * Directions (10 total):
 * - 4 cardinal directions (horizontal): Left, Right, Forward, Back
 * - 4 diagonal directions (horizontal): ForwardLeft, ForwardRight, BackLeft, BackRight
 * - 2 vertical directions: Up, Down
 *
 * @author Tianxiong Zhang
 */
public class ThreeDimensionalBiSpaceSignatureProvider extends DiagonalDirectionalBiSpaceSignatureProvider {

    // Vertical directions (2)
    public static final String UP_ROUTE_TYPE = "UpRoute";
    public static final String DOWN_ROUTE_TYPE = "DownRoute";

    private static ThreeDimensionalBiSpaceSignatureProvider instance;
    private static final Object lock = new Object();

    ThreeDimensionalBiSpaceSignatureProvider() {
        super();
    }

    public static ThreeDimensionalBiSpaceSignatureProvider getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ThreeDimensionalBiSpaceSignatureProvider();
                }
            }
        }
        return instance;
    }

    @Override
    public DynamicSignature getSignature() {
        DynamicSignatureBuilder ext = pureSignatureBuilder();
        ext
                .add(UP_ROUTE_TYPE, 1)
                .add(DOWN_ROUTE_TYPE, 1);

        return BigraphUtil.composeSignatures(super.getSignature(), ext.create());
    }
}
