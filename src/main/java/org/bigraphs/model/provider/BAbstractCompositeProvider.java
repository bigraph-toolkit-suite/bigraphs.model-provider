package org.bigraphs.model.provider;

import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.EMetaModelData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This abstract class specifies the base structure for defining composite classes for all providers ({@link BProvider}):
 * <ul>
 *     <li>{@link BSignatureProvider}</li>
 *     <li>{@link BBigraphProvider}</li>
 *     <li>{@link BRuleProvider}</li>
 * </ul>
 * <p>
 * The class implements some common logic for subclasses:
 * It unifies a common logic like what to do when the {@link EMetaModelData} is different among those.
 * Also, assertions are provided to check if signatures are consistent.
 * <p>
 * A composite contains all the individual parts that make up the class.
 * For example, two signature providers can be combined.
 * The subclass implements the specific operations to combine these.
 *
 * @param <S>
 * @param <C>
 * @author Dominik Grzelak
 */
public abstract class BAbstractCompositeProvider<S extends Signature<?>, R, C extends BProvider>
        extends BAbstractProvider<S> {

    //TODO provide custom merge strategy? default is our merge
    //TODO assert sigConsistent

//    default void mergeNSUri() {}
//    default void mergeEMetaModelData() {}

    protected final List<BAbstractProvider<S>> providers = new ArrayList<>();

    @SafeVarargs
    public BAbstractCompositeProvider(BAbstractProvider<S>... bProviders) {
        this(Arrays.asList(bProviders));

    }

    public BAbstractCompositeProvider(List<BAbstractProvider<S>> bProviders) {
        this.providers.addAll(bProviders);
    }

    public List<BAbstractProvider<S>> getBProviderParts() {
        return providers;
    }

}
