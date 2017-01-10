package fr.cnrs.sharp.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import fr.cnrs.sharp.reasoning.Unification;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
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
public class ProvStoreExpeTest {

    private static final Logger logger = Logger.getLogger(ProvStoreExpeTest.class);

    public ProvStoreExpeTest() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    @Ignore
    public void hello() throws URISyntaxException {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        WebResource service = client.resource(new URI("http://provenance.ecs.soton.ac.uk/store"));

        ClientResponse responseHist = service.path("/api/v0/documents").queryParam("limit", "1").queryParam("created_at__range", "2016-01-01,2016-12-01").accept("application/json").type("application/json").get(ClientResponse.class);
        String rH = responseHist.getEntity(String.class);

        JsonArray arrayDocs = new JsonParser().parse(rH).getAsJsonObject().get("objects").getAsJsonArray();
        for (JsonElement eH : arrayDocs) {
            String id = eH.getAsJsonObject().get("id").getAsString();
            System.out.println(id);

        }

    }

    @Test
    public void wildProvExpe() throws IOException {
        String prefix = "/Users/gaignard-a/Documents/Publis/eswc-2017/provenance-reasoning-mine/experiments/sample-prov-store-docs";

//        String[] ids = {"113207","113206","113263"};
//        for (String id : ids) {
//            Path p = Paths.get(prefix + "/"+id+".ttl"); 
//            System.out.println("Working on "+id);
//            harmonizeProvNoLog(p);
//        }
//        Path p = Paths.get(prefix + "/113207.ttl"); //small
//        Path p = Paths.get(prefix + "/113206.ttl"); //medium
        Path p = Paths.get(prefix + "/113263.ttl"); //large
//        harmonizeProvNoLog(p);
        harmonizeProv(p);

    }

    public void dumpPredStats(Model m) {
        String topPredicatesQuery = "SELECT  ?p (COUNT(?s) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY DESC(?count) LIMIT 10";
        Query query = QueryFactory.create(topPredicatesQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, m);
        ResultSet results = qexec.execSelect();
        System.out.println(ResultSetFormatter.asText(results));
    }

    public void harmonizeProvNoLog(Path inputProv) throws IOException {

        StopWatch sw1 = new StopWatch();
        sw1.start();

        Model data = FileManager.get().loadModel(inputProv.toString());

        /// STEP 1 : OWL sameAs inferences
        Model schema = ModelFactory.createDefaultModel();
        RDFDataMgr.read(schema, "file:///Users/gaignard-a/Documents/Publis/eswc-2017/provenance-reasoning-mine/experiments/provo.ttl", Lang.TURTLE);
        Reasoner owlReasoner = ReasonerRegistry.getOWLMiniReasoner();
        owlReasoner = owlReasoner.bindSchema(schema);
        InfModel owlModel = ModelFactory.createInfModel(owlReasoner, data);
//        System.out.println(sw1.getTime() + " ms");
//        logger.info("OWL entail : Graph size / BNodes : " + owlModel.size() + "/" + Unification.countBN(owlModel));
//        dumpPredStats(owlModel);

        /// STEP 2.1 : PROV inferences TGD == saturation
        List rules = Rule.rulesFromURL("provRules_all.jena");
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
//        reasoner.setOWLTranslation(true);
//        reasoner.setTransitiveClosureCaching(true);        
        InfModel inferredModel = ModelFactory.createInfModel(reasoner, owlModel);
//        System.out.println(sw2.getTime() + " ms");

        /// STEP 2.2 : PROV inferences EGD == unification
        Model m2 = ModelFactory.createDefaultModel().add(inferredModel);
//        Path pathInfProvTGD = Files.createTempFile("PROV-inf-tgd-", ".ttl");
//        m2.write(new FileWriter(pathInfProvTGD.toFile()), "TTL");
//        System.out.println("PROV inferences file written to " + pathInfProvTGD.toString());
//        logger.info("TGD : Graph size / BNodes with saturated PROV graph: " + m2.size() + "/" + Unification.countBN(m2));
//        dumpPredStats(m2);

//        StopWatch sw3 = new StopWatch();
//        sw3.start();
        int nbSubstitution = 1;
        while (nbSubstitution > 0) {
            // UNIFICATION : 1. finding substitution of existential variables 
            List<Pair<RDFNode, RDFNode>> toBeMerged = Unification.selectSubstitutions(m2);
            nbSubstitution = toBeMerged.size();
//                logger.info("Found substitutions: " + nbSubstitution);
            if (toBeMerged.size() > 0) {
                // UNIFICATION : 2. effectively replacing blank nodes by matching nodes
                for (Pair<RDFNode, RDFNode> p : toBeMerged) {
//            logger.debug("Substituting " + p.getLeft().toString() + " with " + p.getRight().toString());
                    Unification.mergeNodes(p.getLeft(), p.getRight().asResource());
                }
                nbSubstitution = Unification.selectSubstitutions(m2).size();
//                    logger.info(nbSubstitution + " found after merging");
            }
        }
        System.out.println(sw1.getTime() + " ms");
//        logger.info("Unification done");
//        logger.info("EGD : Graph size / BNodes with unified PROV inferences: " + m2.size() + "/" + Unification.countBN(m2));
//        Path pathInfProv = Files.createTempFile("PROV-inf-tgd-egd-", ".ttl");
//        m2.write(new FileWriter(pathInfProv.toFile()), "TTL");
//        System.out.println("PROV inferences file written to " + pathInfProv.toString());
//        dumpPredStats(m2);
    }

    public void harmonizeProv(Path inputProv) throws IOException {

        Model data = FileManager.get().loadModel(inputProv.toString());
        logger.info("Asserted graph : Graph size / BNodes : " + data.size() + "/" + Unification.countBN(data));
        dumpPredStats(data);

//        Path pathProv = Files.createTempFile("PROV-", ".ttl");
//        data.write(new FileWriter(pathProv.toFile()), "TTL");
//        System.out.println("PROV file written to " + pathProv.toString());
        /// STEP 1 : OWL sameAs inferences
        Model schema = ModelFactory.createDefaultModel();
        RDFDataMgr.read(schema, "file:///Users/gaignard-a/Documents/Publis/eswc-2017/provenance-reasoning-mine/experiments/provo.ttl", Lang.TURTLE);
        Reasoner owlReasoner = ReasonerRegistry.getOWLMiniReasoner();
        owlReasoner = owlReasoner.bindSchema(schema);
        InfModel owlModel = ModelFactory.createInfModel(owlReasoner, data);
//        logger.info("OWL entail : Graph size / BNodes : " + owlModel.size() + "/" + Unification.countBN(owlModel));
//        dumpPredStats(owlModel);

        /// STEP 2.1 : PROV inferences TGD == saturation
        List rules = Rule.rulesFromURL("provRules_all.jena");
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
//        reasoner.setOWLTranslation(true);
//        reasoner.setTransitiveClosureCaching(true);        
        InfModel inferredModel = ModelFactory.createInfModel(reasoner, owlModel);

        /// STEP 2.2 : PROV inferences EGD == unification
        Model m2 = ModelFactory.createDefaultModel().add(inferredModel);
//        Path pathInfProvTGD = Files.createTempFile("PROV-inf-tgd-", ".ttl");
//        m2.write(new FileWriter(pathInfProvTGD.toFile()), "TTL");
//        System.out.println("PROV inferences file written to " + pathInfProvTGD.toString());
//        logger.info("TGD : Graph size / BNodes with saturated PROV graph: " + m2.size() + "/" + Unification.countBN(m2));
//        dumpPredStats(m2);

//        StopWatch sw3 = new StopWatch();
//        sw3.start();
        int nbSubstitution = 1;
        while (nbSubstitution > 0) {
            // UNIFICATION : 1. finding substitution of existential variables 
            List<Pair<RDFNode, RDFNode>> toBeMerged = Unification.selectSubstitutions(m2);
            nbSubstitution = toBeMerged.size();
//                logger.info("Found substitutions: " + nbSubstitution);
            if (toBeMerged.size() > 0) {
                // UNIFICATION : 2. effectively replacing blank nodes by matching nodes
                for (Pair<RDFNode, RDFNode> p : toBeMerged) {
//            logger.debug("Substituting " + p.getLeft().toString() + " with " + p.getRight().toString());
                    Unification.mergeNodes(p.getLeft(), p.getRight().asResource());
                }
                nbSubstitution = Unification.selectSubstitutions(m2).size();
//                    logger.info(nbSubstitution + " found after merging");
            }
        }
//        System.out.println(sw3.getTime() + " ms");
        logger.info("OWL+TGD+EGD : Graph size / BNodes : " + m2.size() + "/" + Unification.countBN(m2));
        Path pathInfProv = Files.createTempFile("PROV-inf-tgd-egd-", ".ttl");
        m2.write(new FileWriter(pathInfProv.toFile()), "TTL");
        System.out.println("PROV inferences file written to " + pathInfProv.toString());
        dumpPredStats(m2);
    }

}
