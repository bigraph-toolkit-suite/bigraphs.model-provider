package org.bigraphs.model.provider.spatial.signature;

import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.model.provider.base.BAbstractSignatureProvider;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * The default signature for the CF-MAPF Use Case.
 * This represents the location syntax.
 *
 * @author Dominik Grzelak
 */
public class BiSpaceSignatureProvider extends BAbstractSignatureProvider<DefaultDynamicSignature> {

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
    public DefaultDynamicSignature getSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .addControl("VarSpace", 0)
                .addControl("True", 0)
                .addControl("False", 0)
                .addControl("Error", 0)
                .addControl("Locale", 1)
                .addControl("Route", 1)
        ;
        return defaultBuilder.create();
    }
}
