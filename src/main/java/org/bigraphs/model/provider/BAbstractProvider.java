package org.bigraphs.model.provider;

import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.EMetaModelData;

/**
 * Abstract implementation of the most general interface {@link BProvider} that implements the minimal set of properties
 * for every bigraphical model (e.g., signatures, host bigraphs, rules, composite structures).
 *
 * @param <S>
 * @author Dominik Grzelak
 */
public abstract class BAbstractProvider<S extends Signature<?>> implements BProvider, BModelProviderSupport {

    protected String NSURI = "org.bigraphs";
    protected EMetaModelData META_MODEL_DATA = EMetaModelData.builder().setName("bModelName").setNsPrefix("bigraphMetaModel").setNsUri(NSURI).create();

    protected boolean LOG_DEBUG = false;


    @Override
    public String getNsUri() {
        return NSURI;
    }

    @Override
    public EMetaModelData getMetaModelData() {
        return META_MODEL_DATA;
    }
}
