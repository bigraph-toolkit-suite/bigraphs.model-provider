package org.bigraphs.model.provider;

import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.EMetaModelData;


public abstract class BAbstractProvider<S extends Signature<?>> implements BProvider, BModelProviderSupport {

//    protected String NSURI = "org.bigraphs.example.pacman";
//    protected EMetaModelData META_MODEL_DATA = EMetaModelData.builder().setName("bpacman").setNsPrefix("bigraphMetaModel").setNsUri(NSURI).create();
    protected String NSURI = "org.bigraphs";
    protected EMetaModelData META_MODEL_DATA = EMetaModelData.builder().setName("modelName").setNsPrefix("bigraphMetaModel").setNsUri(NSURI).create();

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
