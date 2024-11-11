package org.bigraphs.model.provider.base;

import org.bigraphs.model.provider.BAbstractProvider;
import org.bigraphs.model.provider.BBigraphProvider;
import org.bigraphs.model.provider.BSignatureProvider;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Abstract class for any host bigraph model provider implementation.
 *
 * @param <S>
 * @param <B>
 * @author Dominik Grzelak
 */
public abstract class BAbstractBigraphProvider<S extends Signature<?>, B extends Bigraph<S>>
        extends BAbstractProvider<S> implements BBigraphProvider<S, B> {

    protected BSignatureProvider<S> signatureProvider;


    //TODO notnull
    public BAbstractBigraphProvider(BSignatureProvider<S> signatureProvider) {
        this.signatureProvider = signatureProvider;
    }

    @Override
    public BSignatureProvider<S> getSignatureProvider() {
        return signatureProvider;
    }
}
