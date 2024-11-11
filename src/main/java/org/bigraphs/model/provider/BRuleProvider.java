package org.bigraphs.model.provider;


import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;

/**
 * The most general interface for all sorts of bigraphical rule providers (file-based, database-driven, dynamically, ...).
 *
 * @param <S> type of the signature used by the bigraph
 * @param <R> type of the bigraphical reaction rule that is finally produced
 * @author Dominik Grzelak
 */
public interface BRuleProvider<S extends Signature<?>, B extends Bigraph<S>, R extends ReactionRule<B>>
        extends BSignatureProvider<S> {

    R getRule() throws Exception;

    S getSignature();
}
