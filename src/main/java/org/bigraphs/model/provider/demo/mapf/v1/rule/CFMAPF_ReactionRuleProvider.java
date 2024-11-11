package org.bigraphs.model.provider.demo.mapf.v1.rule;

import lombok.Getter;
import lombok.Setter;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.TrackingMap;
import org.bigraphs.model.provider.base.BAbstractRuleProvider;
import org.bigraphs.model.provider.base.BLocationModelData;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * Demo bigraph model provider for the Crazyflie MAPF Use Case (2D only).
 * <p>
 * It takes a nxn bigrid and creates the complete view with navigation elements and agents.
 * The individual layers can be also returned: location, navigation, agents
 */
public class CFMAPF_ReactionRuleProvider extends BAbstractRuleProvider<DefaultDynamicSignature, PureBigraph, ParametricReactionRule<PureBigraph>> {

    private BLocationModelData lmpd;
    private DefaultDynamicSignature extendedSignature;
    String label;
    BLocationModelData.Agent agent;
    @Getter
    @Setter
    protected String suffixId = ""; // for unique outer names

    public CFMAPF_ReactionRuleProvider(BLocationModelData lmpd, String label, DefaultDynamicSignature signature) {
        super(EMPTY_DYNAMIC_SIGNATURE_PUREBIGRAPH);
        this.lmpd = lmpd;
        this.label = label;
        this.extendedSignature = signature;

    }

    @Override
    public DefaultDynamicSignature getSignature() {
        return extendedSignature;
    }

    // refine the rule for a specific agent
    public CFMAPF_ReactionRuleProvider forAgent(BLocationModelData.Agent agent) {
        this.agent = agent;
        return this;
    }

    /**
     * Creates a bigrid with "atomic locales": containing only one coordinate assigned the center of the locale.
     *
     * @return
     * @throws Exception
     */
    @Override
    public ParametricReactionRule<PureBigraph> getRule() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(getSignature());
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(getSignature());

        String targetPrefixLabel = "targetPrefixLabel" + getSuffixId();
        if (agent != null) targetPrefixLabel = lmpd.getTargetPrefixLabel(agent) + getSuffixId();

        BigraphEntity.OuterName fromD = builder.createOuterName("destination"+getSuffixId());
        BigraphEntity.OuterName fromS = builder.createOuterName("source"+getSuffixId());

        builder.createRoot()
                .addChild("Locale").linkToOuter(fromS).down()
                /**/.addSite()
                /**/.addChild("CO", "coord"+getSuffixId()).down().addSite().up()
                /**/.addChild("Ensemble").down().addSite().addChild("Agent", "coord"+getSuffixId()).linkToOuter(targetPrefixLabel).down()
                /**//**//**/.addSite().up().up()
                /**/.addChild("Route").linkToOuter(fromD)
                /**//**/.top()
                .addChild("Locale").linkToOuter(fromD).down()
                /**/.addChild("Ensemble").down().addSite().up()
                /**/.addChild("CO", "coordTarget"+getSuffixId()).down().addSite().up()
                /**/.addSite()
                .top()
        ;

        BigraphEntity.OuterName fromD2 = builder2.createOuterName("destination"+getSuffixId());
        BigraphEntity.OuterName fromS2 = builder2.createOuterName("source"+getSuffixId());

        builder2.createOuterName("coord"+getSuffixId());
        builder2.createRoot()
                .addChild("Locale").linkToOuter(fromS2).down()
                /**/.addSite()
                /**/.addChild("CO", "coord" + getSuffixId()).down().addSite().up()
                /**/.addChild("Ensemble").down().addSite().up()
                /**/.addChild("Route").linkToOuter(fromD2).top()
                .addChild("Locale").linkToOuter(fromD2).down()
                /**/.addChild("Ensemble").down().addChild("Agent", "coordTarget"+getSuffixId()).linkToOuter(targetPrefixLabel).down()
                /**//**/.addSite().up().addSite().up()
                /**/.addChild("CO", "coordTarget"+getSuffixId()).down()
                /**//**/.addSite().up().addSite().top()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
//        InstantiationMap instantiationMap = InstantiationMap.create(3);
        ParametricReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        ((ParametricReactionRule<?>) rr).withLabel(label);
        TrackingMap map = new TrackingMap(); // reactum -> redex
        map.put("v0", "v0");
        map.put("v1", "v1");
        map.put("v2", "v2");
        map.put("v3", "v4");
        map.put("v4", "v5");
        map.put("v5", "v6");
        map.put("v6", "v3");
        map.put("v7", "v7");
        map.addLinkNames(redex.getOuterNames().stream().map(BigraphEntity.Link::getName).toArray(String[]::new));
        rr.withTrackingMap(map);
        return rr;
    }

}
