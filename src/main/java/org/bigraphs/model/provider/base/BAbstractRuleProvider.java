package org.bigraphs.model.provider.base;

import lombok.Getter;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.model.provider.BAbstractProvider;
import org.bigraphs.model.provider.BRuleProvider;
import org.bigraphs.model.provider.BSignatureProvider;

/**
 * Abstract class for any rule provider implementation.
 *
 * @param <S>
 * @param <B>
 * @param <R>
 * @author Dominik Grzelak
 */
@Getter
public abstract class BAbstractRuleProvider<S extends Signature<?>, B extends Bigraph<S>, R extends ReactionRule<B>>
        extends BAbstractProvider<S>
        implements BRuleProvider<S, B, R> {

    protected BSignatureProvider<S> signatureProvider;

    //TODO notnull
    public BAbstractRuleProvider(BSignatureProvider<S> signatureProvider) {
        this.signatureProvider = signatureProvider;
    }

}
