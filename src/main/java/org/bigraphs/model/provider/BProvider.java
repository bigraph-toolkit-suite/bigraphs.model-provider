package org.bigraphs.model.provider;


import org.bigraphs.framework.core.datatypes.EMetaModelData;

/**
 * Generic base interface for all providers of any bigraph model types (signatures, host bigraphs, rules).
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
