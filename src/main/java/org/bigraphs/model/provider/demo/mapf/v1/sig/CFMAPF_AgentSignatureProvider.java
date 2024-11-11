package org.bigraphs.model.provider.demo.mapf.v1.sig;

import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.model.provider.base.BAbstractSignatureProvider;

import static org.bigraphs.framework.core.factory.BigraphFactory.createOrGetSignature;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * The default signature for the CF-MAPF Use Case.
 * This represents the minimal agent syntax.
 *
 * @author Dominik Grzelak
 */
public class CFMAPF_AgentSignatureProvider extends BAbstractSignatureProvider<DefaultDynamicSignature> {

    private static CFMAPF_AgentSignatureProvider instance;
    private static final Object lock = new Object();

    private CFMAPF_AgentSignatureProvider() {
    }

    public static CFMAPF_AgentSignatureProvider getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new CFMAPF_AgentSignatureProvider();
                }
            }
        }
        return instance;
    }

    @Override
    public DefaultDynamicSignature getSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .addControl("Agent", 2)
                .addControl("ID", 0)
                .addControl("N_1", 0)
                .addControl("N_2", 0)
                .addControl("Idle", 0)
                .addControl("Status", 0)
                .addControl("Active", 0)
                .addControl("Stop", 0)
        ;
        DefaultDynamicSignature sig = defaultBuilder.create();
        //TODO call createOrGetSignature and init with EMetaData?
//        createOrGetSignature(sig.getInstanceModel(), getMetaModelData());
        return sig;
    }
}
