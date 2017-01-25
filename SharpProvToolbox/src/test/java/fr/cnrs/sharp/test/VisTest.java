/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.sharp.test;

import fr.cnrs.sharp.Util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import static org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler.logger;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Alban Gaignard <alban.gaignard@univ-nantes.fr>
 */
public class VisTest {

    public VisTest() {
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
    public void hello() throws IOException {
        String inputGraph = "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
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

        Model data = ModelFactory.createDefaultModel();
        InputStream stream = new ByteArrayInputStream(inputGraph.getBytes(StandardCharsets.UTF_8));
        RDFDataMgr.read(data, stream, Lang.TTL);

        Util.writeHtmlViz(data);
    }
}
