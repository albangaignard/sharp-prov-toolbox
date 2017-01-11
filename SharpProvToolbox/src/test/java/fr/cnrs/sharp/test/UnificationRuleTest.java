package fr.cnrs.sharp.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.cnrs.sharp.reasoning.Harmonization;
import fr.cnrs.sharp.reasoning.Unification;
import fr.cnrs.sharp.reasoning.Util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class UnificationRuleTest {

    private static final Logger logger = Logger.getLogger(UnificationRuleTest.class);

    private String inputGraph = "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
            + "@prefix : <http://example.org/> ."
            + "@prefix prov: <http://www.w3.org/ns/prov#> .\n"
            + ":a2 "
            + "     prov:used      :e ; \n"
            + "     prov:used      _:BN . \n"
            + ":e \n"
            + "      a       prov:Entity ;\n"
            + "      prov:wasGeneratedBy :a1 ."
            + "_:BN \n"
            + "      a       prov:SubEntity ;\n"
            + "      :label \"label bn 2\" ;"
            + "     prov:wasGeneratedBy :a1 ;"
            + "     prov:wasGeneratedBy :a4 ."
            + ":a3 "
            + "     prov:used _:BN ."
            + ""
            + "_:bn2 :rel1 :z ."
            + ":y :rel1 :z ."
            + ""
            + "_:bn3 :rel1 :za ."
            + ":ya :rel2 :za .";

    public UnificationRuleTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testUnification() {
        Model data = ModelFactory.createDefaultModel();
        InputStream stream = new ByteArrayInputStream(inputGraph.getBytes(StandardCharsets.UTF_8));
        RDFDataMgr.read(data, stream, Lang.TTL);
        data.write(System.out, "TTL");
        logger.info("Graph size / BNodes : " + data.size() + "/" + Unification.countBN(data));

        Assert.assertEquals(13, data.size());
        Assert.assertEquals(3, Unification.countBN(data));

        int nbSubst = 1;
        while (nbSubst > 0) {
            // UNIFICATION : 1. select substitutions
            List<Pair<RDFNode, RDFNode>> toBeMerged = Unification.selectSubstitutions(data);
            nbSubst = toBeMerged.size();
            logger.info("Found substitutions: " + nbSubst);

            // UNIFICATION : 2. effectively replacing blank nodes by matching nodes
            for (Pair<RDFNode, RDFNode> p : toBeMerged) {
                Unification.mergeNodes(p.getLeft(), p.getRight().asResource());
            }
            logger.info("Graph size / BNodes with unified PROV inferences: " + data.size() + "/" + Unification.countBN(data));

            nbSubst = Unification.selectSubstitutions(data).size();
            logger.info("Found substitutions: " + nbSubst + " after merging");
        }
        data.write(System.out, "TTL");

        Assert.assertEquals(10, data.size());
        Assert.assertEquals(1, Unification.countBN(data));
    }

    @Test
    public void testFullHarmonization() throws IOException {
        Model data = ModelFactory.createDefaultModel();
        InputStream stream = new ByteArrayInputStream(inputGraph.getBytes(StandardCharsets.UTF_8));
        RDFDataMgr.read(data, stream, Lang.TTL);
        data.write(System.out, "TTL");
        logger.info("Graph size / BNodes : " + data.size() + "/" + Unification.countBN(data));

        Assert.assertEquals(13, data.size());
        Assert.assertEquals(3, Unification.countBN(data));

        Model hData = Harmonization.harmonizeProv(data);
        logger.info("Harmonized graph size / BNodes : " + hData.size() + "/" + Unification.countBN(hData));

//        Assert.assertEquals(2863, hData.size()); // non-deterministic data size !!
        Assert.assertEquals(3, Unification.countBN(hData)); // !! should be 1 ? !!
        Util.describeBlankNodes(data);
        Util.describeBlankNodes(hData);
    }
}
