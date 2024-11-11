package org.bigraphs.model.provider;


import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Most general interface for all sorts of host bigraph model providers (file-based, database-driven, dynamically, ...).
 *
 * @param <S> type of the signature used by the bigraph
 * @param <B> type of the bigraph that is finally produced
 * @author Dominik Grzelak
 */
public interface BBigraphProvider<S extends Signature<?>, B extends Bigraph<S>> extends BSignatureProvider<S> {

    B getBigraph() throws Exception;

    S getSignature();


    // TODO move this to BBigraphProvider?
    BSignatureProvider<S> getSignatureProvider();
}
