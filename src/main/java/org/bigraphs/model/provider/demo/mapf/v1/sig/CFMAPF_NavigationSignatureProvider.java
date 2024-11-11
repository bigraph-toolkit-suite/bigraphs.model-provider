package org.bigraphs.model.provider.demo.mapf.v1.sig;

import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.model.provider.base.BAbstractSignatureProvider;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * The signature for the CF-MAPF Use Case.
 * This represents the navigation syntax.
 *
 * @author Dominik Grzelak
 */
public class CFMAPF_NavigationSignatureProvider extends BAbstractSignatureProvider<DefaultDynamicSignature> {

    private static CFMAPF_NavigationSignatureProvider instance;
    private static final Object lock = new Object();

    private CFMAPF_NavigationSignatureProvider() {
    }

    public static CFMAPF_NavigationSignatureProvider getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new CFMAPF_NavigationSignatureProvider();
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
                .addControl("Ensemble", 0)
                .addControl("CO", 1)
                .addControl("NE", 1)
                .addControl("tgt", 2)
        ;
        return defaultBuilder.create();
    }
}
