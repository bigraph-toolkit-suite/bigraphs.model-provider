package org.bigraphs.model.provider;


import org.bigraphs.framework.core.datatypes.EMetaModelData;

/**
 * Most generic base interface for all providers for every element of a bigraphical model
 * (signatures, host bigraphs, rules, composite structures).
 * <p>
 * The implementation-specific details are omitted here as well, i.e., whether the models are
 * file-based, or are loaded from a database, or created dynamically at runtime, etc.
 *
 * @author Dominik Grzelak
 */
public interface BProvider {

    String getNsUri();

    EMetaModelData getMetaModelData();
}
