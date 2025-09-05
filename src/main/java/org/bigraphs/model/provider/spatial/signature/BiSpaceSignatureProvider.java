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
                .add("VarSpace", 0)
                .add("True", 0)
                .add("False", 0)
                .add("Error", 0)
                .add("Locale", 1)
                .add("Route", 1)
        ;
        return defaultBuilder.create();
    }
}
