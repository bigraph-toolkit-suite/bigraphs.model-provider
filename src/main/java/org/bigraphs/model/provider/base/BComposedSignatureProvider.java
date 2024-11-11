package org.bigraphs.model.provider.base;

import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.model.provider.BAbstractProvider;
import org.bigraphs.model.provider.BAbstractCompositeProvider;
import org.bigraphs.model.provider.BSignatureProvider;

import java.util.*;
import java.util.function.Supplier;

/**
 * This composite class for {@link BSignatureProvider} builds the merge product of all given signature provider
 * instances.
 * <p>
 * This implements the composite pattern for the {@link BSignatureProvider} within
 * the {@link org.bigraphs.model.provider.BProvider} class hierarchy.
 *
 * @author Dominik Grzelak
 */
public class BComposedSignatureProvider<S extends Signature<?>, R, C extends BSignatureProvider<S>>
        extends BAbstractCompositeProvider<S, R, C>
        implements BSignatureProvider<S> {

    protected S composite = null;
    protected boolean invalidate = false;

    //extends    BSignatureProvider<S extends Signature<?>, B extends Bigraph<S>>
    public BComposedSignatureProvider(BSignatureProvider<S>... bSignatureProviders) {
        this(Arrays.asList(bSignatureProviders));
    }

    public BComposedSignatureProvider(List<BSignatureProvider<S>> bSignatureProviders) {
        super(bSignatureProviders.stream().map(x -> (BAbstractProvider<S>) x).toList());
    }


    //TODO assertSigConsistent: call from super
    @Override
    public S getSignature() {
        if (composite != null && !invalidate) return composite;
        try {
            Optional<S> accumulatedResult = (Optional<S>) getBProviderParts().stream()
                    .map(s -> (DefaultDynamicSignature) ((BSignatureProvider<S>) s).getSignature())
                    .reduce(BigraphUtil::mergeSignatures);
            composite = accumulatedResult.orElseThrow(new Supplier<Throwable>() {
                @Override
                public Throwable get() {
                    return new RuntimeException("Merging signatures failed");
                }
            });
            return composite;
        } catch (Throwable e) {
            throw new RuntimeException("Not able to compute signature merge product other than for DefaultDynamicSignature objects", e);
        }
    }
}