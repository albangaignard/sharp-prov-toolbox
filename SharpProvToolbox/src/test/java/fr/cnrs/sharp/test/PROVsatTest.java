package fr.cnrs.sharp.test;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//import fr.cnrs.ga2prov.reasoning.PROVsat;
import fr.cnrs.sharp.reasoning.Unification;
//import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class PROVsatTest {

    private static final Logger logger = Logger.getLogger(PROVsatTest.class);

    public PROVsatTest() {
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
    public void saturateProvWithJenaRules_V2() throws IOException {
        Model data = FileManager.get().loadModel("/Users/gaignard-a/Documents/Publis/eswc-2017/provenance-reasoning-mine/experiments/asserted-prov/primary-analysis-galaxy.ttl");
        FileManager.get().readModel(data, "/Users/gaignard-a/Documents/Publis/eswc-2017/provenance-reasoning-mine/experiments/asserted-prov/workflowrun.prov.ttl");
        FileManager.get().readModel(data, "/Users/gaignard-a/Documents/Publis/eswc-2017/provenance-reasoning-mine/experiments/sameas/sameas.ttl");
        Path pathProv = Files.createTempFile("PROV-", ".ttl");
        data.write(new FileWriter(pathProv.toFile()), "TTL");
        System.out.println("PROV file written to " + pathProv.toString());

        //// STEP 1 : OWL sameAs inferences
        Model schema = ModelFactory.createDefaultModel();
        RDFDataMgr.read(schema, "file:///Users/gaignard-a/Documents/Publis/eswc-2017/provenance-reasoning-mine/experiments/provo.ttl", Lang.TURTLE);
        Reasoner owlReasoner = ReasonerRegistry.getOWLMiniReasoner();
        owlReasoner = owlReasoner.bindSchema(schema);
        InfModel owlModel = ModelFactory.createInfModel(owlReasoner, data);
        System.out.println("Graph size with OWL entailments: " + owlModel.size());

        //// STEP 2.1 : PROV inferences TGD == saturation
        List rules = Rule.rulesFromURL("provRules_all.jena");
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        InfModel inferredModel = ModelFactory.createInfModel(reasoner, owlModel);

        //// STEP 2.2 : PROV inferences EGD == unification
        Model m2 = ModelFactory.createDefaultModel().add(inferredModel);
        Path pathInfProvTGD = Files.createTempFile("PROV-inf-tgd-", ".ttl");
        m2.write(new FileWriter(pathInfProvTGD.toFile()), "TTL");
        System.out.println("PROV inferences file written to " + pathInfProvTGD.toString());
        
        logger.info("TGD : Graph size / BNodes with saturated PROV graph: " + m2.size() + "/" + Unification.countBN(m2));
        int nbSubstitution = 1;
        while (nbSubstitution > 0) {
            // UNIFICATION : 1. finding substitution of existential variables 
            List<Pair<RDFNode, RDFNode>> toBeMerged = Unification.selectSubstitutions(m2);
            nbSubstitution = toBeMerged.size();
            if (toBeMerged.size() > 0) {
                // UNIFICATION : 2. effectively replacing blank nodes by matching nodes
                for (Pair<RDFNode, RDFNode> p : toBeMerged) {
                    Unification.mergeNodes(p.getLeft(), p.getRight().asResource());
                }
                nbSubstitution = Unification.selectSubstitutions(m2).size();
            }
        }
        logger.info("Unification done");
        logger.info("EGD : Graph size / BNodes with unified PROV inferences: " + m2.size() + "/" + Unification.countBN(m2));

        Path pathInfProv = Files.createTempFile("PROV-inf-tgd-egd-", ".ttl");
        m2.write(new FileWriter(pathInfProv.toFile()), "TTL");
        System.out.println("PROV inferences file written to " + pathInfProv.toString());
    }

}
