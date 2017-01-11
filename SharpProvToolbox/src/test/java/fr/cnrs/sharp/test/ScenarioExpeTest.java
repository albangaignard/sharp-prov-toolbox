package fr.cnrs.sharp.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.cnrs.sharp.Main;
import fr.cnrs.sharp.reasoning.Harmonization;
import fr.cnrs.sharp.reasoning.Unification;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    public void dumpPredStats(Model m) {
        String topPredicatesQuery = "SELECT  ?p (COUNT(?s) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY DESC(?count) LIMIT 10";
        Query query = QueryFactory.create(topPredicatesQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, m);
        ResultSet results = qexec.execSelect();
        System.out.println(ResultSetFormatter.asText(results));
    }

    @Test
    public void multiSiteWFTest() throws IOException {
        Model data = ModelFactory.createDefaultModel();
        RDFDataMgr.read(data, ScenarioExpeTest.class.getClassLoader().getResourceAsStream("galaxy.prov.ttl") , Lang.TTL);
        RDFDataMgr.read(data, ScenarioExpeTest.class.getClassLoader().getResourceAsStream("taverna.prov.ttl") , Lang.TTL);
        RDFDataMgr.read(data, ScenarioExpeTest.class.getClassLoader().getResourceAsStream("sameas.ttl") , Lang.TTL);

        Model res = Harmonization.harmonizeProv(data);
        Assert.assertEquals(10, Unification.countBN(res));
    }
}
