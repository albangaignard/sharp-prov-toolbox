/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.sharp.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Alban Gaignard <alban.gaignard@univ-nantes.fr>
 */
public class SameFilesTest {

    public SameFilesTest() {
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
    public void SameAsGen() throws FileNotFoundException, IOException {
        
        Model data = ModelFactory.createDefaultModel();

        String file1_prov = "@base         <http://fr.symetric> .\n"
                + "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .\n"
                + "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n"
                + "@prefix sioc: <http://rdfs.org/sioc/ns#> .\n"
                + "@prefix prov: <http://www.w3.org/ns/prov#> .\n"
                + "@prefix sym:   <http://fr.symetric/vocab#> .\n"
                + "@prefix dcterms: <http://purl.org/dc/terms/> .\n"
                + "@prefix tavernaprov:  <http://ns.taverna.org.uk/2012/tavernaprov/> .\n"
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "<#c583bef6-de69-4caa-bc3a-1bc1136543b4>\n"
                + "	 a prov:Entity ;\n"
                + "	 rdfs:label \"access.log-20150926.bz2\"^^xsd:String ;\n"
                + "	 tavernaprov:sha512 \"1d305986330304378f82b938d776ea0be48eda8210f7af6c152e8562cf6393b2f5edd452c22ef6fe8c729cb01eb3687ac35f1c5e57ddefc46276e9c60409276a\"^^xsd:String .";

        String alt_file1_prov = "@base         <http://fr.symetric> .\n"
                + "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .\n"
                + "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n"
                + "@prefix sioc: <http://rdfs.org/sioc/ns#> .\n"
                + "@prefix prov: <http://www.w3.org/ns/prov#> .\n"
                + "@prefix sym:   <http://fr.symetric/vocab#> .\n"
                + "@prefix dcterms: <http://purl.org/dc/terms/> .\n"
                + "@prefix tavernaprov:  <http://ns.taverna.org.uk/2012/tavernaprov/> .\n"
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "<#c583bef6-de69-4caa-bc3a-00000000>\n"
                + "	 a prov:Entity ;\n"
                + "	 rdfs:label \"access.log-20150926-copy.bz2\"^^xsd:String ;\n"
                + "	 tavernaprov:sha512 \"1d305986330304378f82b938d776ea0be48eda8210f7af6c152e8562cf6393b2f5edd452c22ef6fe8c729cb01eb3687ac35f1c5e57ddefc46276e9c60409276a\"^^xsd:String .";

        String constructSameAs = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                + "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
                + "PREFIX tavernaprov:  <http://ns.taverna.org.uk/2012/tavernaprov/> \n"
                + "PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"
                + "CONSTRUCT { \n"
                + "    ?x owl:sameAs ?y ."
                + "} WHERE {\n"
                + "    ?x a prov:Entity .\n"
                + "    ?x tavernaprov:sha512 ?x_sha512 .\n"
                + "    ?y a prov:Entity .\n"
                + "    ?y tavernaprov:sha512 ?y_sha512 .\n"
                + "    FILTER( ?x_sha512 = ?y_sha512 ) .\n"
                + "}";
        
        RDFDataMgr.read(data, new StringReader(file1_prov),null,Lang.TTL);
        RDFDataMgr.read(data, new StringReader(alt_file1_prov),null,Lang.TTL);
        data.write(System.out, "TTL");
        
        Query query = QueryFactory.create(constructSameAs);
        QueryExecution queryExec = QueryExecutionFactory.create(query, data);
        Model sameAsModel = queryExec.execConstruct();
        queryExec.close();
        sameAsModel.write(System.out, "TTL");

    }
}
