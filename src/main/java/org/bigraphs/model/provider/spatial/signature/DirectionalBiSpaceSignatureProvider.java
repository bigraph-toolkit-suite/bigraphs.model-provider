package org.bigraphs.model.provider.spatial.signature;

import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.utils.BigraphUtil;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * A signature provider for directional bi-spatial bigraphs.
 * Extends the basic signature with directional route types:
 * - LeftRoute: connects to the left neighbor
 * - RightRoute: connects to the right neighbor
 * - ForwardRoute: connects to the forward (up) neighbor
 * - BackRoute: connects to the back (down) neighbor
 *
 * @author Tianxiong Zhang (Main)
 * @author Dominik Grzelak (Contributor)
 */
public class DirectionalBiSpaceSignatureProvider extends BiSpaceSignatureProvider {

    // Cardinal directions (4)
    public static final String LEFT_ROUTE_TYPE = "LeftRoute";
    public static final String RIGHT_ROUTE_TYPE = "RightRoute";
    public static final String FORWARD_ROUTE_TYPE = "ForwardRoute";
    public static final String BACK_ROUTE_TYPE = "BackRoute";

    private static DirectionalBiSpaceSignatureProvider instance;
    private static final Object lock = new Object();

    DirectionalBiSpaceSignatureProvider() {
        super();
    }

    public static DirectionalBiSpaceSignatureProvider getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DirectionalBiSpaceSignatureProvider();
                }
            }
        }
        return instance;
    }

    @Override
    public DynamicSignature getSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .add(LEFT_ROUTE_TYPE, 1)
                .add(RIGHT_ROUTE_TYPE, 1)
                .add(FORWARD_ROUTE_TYPE, 1)
                .add(BACK_ROUTE_TYPE, 1);
        return BigraphUtil.composeSignatures(super.getSignature(), defaultBuilder.create());
    }
}