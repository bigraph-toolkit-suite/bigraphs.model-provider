package org.bigraphs.model.provider.base;

import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.model.provider.BAbstractProvider;
import org.bigraphs.model.provider.BSignatureProvider;

/**
 * Abstract class for any signature provider implementation.
 * <p>
 * It can also function as a decorator taking another {@link BSignatureProvider} via the overloaded
 * constructor.
 *
 * @param <S>
 * @author Dominik Grzelak
 */
public abstract class BAbstractSignatureProvider<S extends Signature<?>>
        extends BAbstractProvider<S>
        implements BSignatureProvider<S> {

    BSignatureProvider<S> delegate;

    public BAbstractSignatureProvider() {
    }

    public BAbstractSignatureProvider(BSignatureProvider<S> delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getNsUri() {
        if (delegate != null) return delegate.getNsUri();
        return super.getNsUri();
    }

    @Override
    public EMetaModelData getMetaModelData() {
        if (delegate != null) return delegate.getMetaModelData();
        return super.getMetaModelData();
    }
}
