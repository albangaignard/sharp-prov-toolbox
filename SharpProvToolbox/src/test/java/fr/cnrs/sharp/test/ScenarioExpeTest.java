package fr.cnrs.sharp.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.cnrs.sharp.Util;
import fr.cnrs.sharp.reasoning.Harmonization;
import fr.cnrs.sharp.reasoning.Unification;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
public class ScenarioExpeTest {

    private static final Logger logger = Logger.getLogger(ScenarioExpeTest.class);

    public ScenarioExpeTest() {
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
//    @Ignore
    public void multiSiteWFTest() throws IOException {
        Model data = ModelFactory.createDefaultModel();
        RDFDataMgr.read(data, ScenarioExpeTest.class.getClassLoader().getResourceAsStream("galaxy.prov.ttl"), Lang.TTL);
        RDFDataMgr.read(data, ScenarioExpeTest.class.getClassLoader().getResourceAsStream("taverna.prov.ttl"), Lang.TTL);
        RDFDataMgr.read(data, ScenarioExpeTest.class.getClassLoader().getResourceAsStream("sameas.ttl"), Lang.TTL);

        Model res = Harmonization.harmonizeProv(data);
        Util.dumpPredStats(res);
        Assert.assertEquals(10, Unification.countBN(res));
    }
    
    @Test
    public void multiSiteSummaryTest() throws IOException {
        Model data = ModelFactory.createDefaultModel();
        RDFDataMgr.read(data, ScenarioExpeTest.class.getClassLoader().getResourceAsStream("galaxy.prov.ttl"), Lang.TTL);
        RDFDataMgr.read(data, ScenarioExpeTest.class.getClassLoader().getResourceAsStream("taverna.prov.ttl"), Lang.TTL);
        RDFDataMgr.read(data, ScenarioExpeTest.class.getClassLoader().getResourceAsStream("sameas.ttl"), Lang.TTL);

        Model res = Harmonization.harmonizeProv(data);
        Assert.assertEquals(10, Unification.countBN(res));

        Path pathInfProv = Files.createTempFile("PROV-inf-tgd-egd-", ".ttl");
        res.write(new FileWriter(pathInfProv.toFile()), "TTL");
        logger.info("PROV inferences file written to " + pathInfProv.toString());

        String queryInfluence = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                + "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
                + "CONSTRUCT { \n"
                + "    ?x ?p ?y .\n"
                + "    ?x rdfs:label ?lx .\n"
                + "    ?y rdfs:label ?ly .\n"
                + "} WHERE {\n"
                + "    ?x ?p ?y .\n"
                + "    FILTER (?p IN (prov:wasInfluencedBy)) .\n"
                + "    ?x rdfs:label ?lx .\n"
                + "    ?y rdfs:label ?ly .\n"
                + "}";

        Query query = QueryFactory.create(queryInfluence);
        QueryExecution queryExec = QueryExecutionFactory.create(query, res);
        Model summary = queryExec.execConstruct();
        queryExec.close();

        Assert.assertTrue(summary.size() > 200);
        Assert.assertEquals(0, Unification.countBN(summary));

        Util.writeHtmlViz(summary);
    }
}
