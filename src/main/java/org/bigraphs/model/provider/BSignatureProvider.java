package org.bigraphs.model.provider;

import org.bigraphs.framework.core.HasSignature;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * The most general interface of any signature provider (file-based, database-driven, dynamic, ...).
 *
 * @param <S> type of the signature used by the bigraph
 * @author Dominik Grzelak
 */
public interface BSignatureProvider<S extends Signature<?>> extends BProvider, HasSignature<S> {
    S getSignature();

    BSignatureProvider<DynamicSignature> EMPTY_DYNAMIC_SIGNATURE_PUREBIGRAPH = new BSignatureProvider<>() {
        @Override
        public String getNsUri() {
            return "org.bigraphs.empty";
        }

        @Override
        public EMetaModelData getMetaModelData() {
            return EMetaModelData.builder().setNsUri(getNsUri()).setName("empty").setNsPrefix("bigraphMetaModel").create();
        }

        @Override
        public DynamicSignature getSignature() {
            return pureSignatureBuilder().create(getMetaModelData());
        }
    };
}
