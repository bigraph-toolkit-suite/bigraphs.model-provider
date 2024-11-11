package org.bigraphs.model.provider.demo.mapf.v1.sig;

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
public class CFMAPF_LocationSignatureProvider extends BAbstractSignatureProvider<DefaultDynamicSignature> {

    private static CFMAPF_LocationSignatureProvider instance;
    private static final Object lock = new Object();

    private CFMAPF_LocationSignatureProvider() {
    }

    public static CFMAPF_LocationSignatureProvider getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new CFMAPF_LocationSignatureProvider();
                }
            }
        }
        return instance;
    }

    @Override
    public DefaultDynamicSignature getSignature() {
        //TODO call createOrGetSignature and init with EMetaData?
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
