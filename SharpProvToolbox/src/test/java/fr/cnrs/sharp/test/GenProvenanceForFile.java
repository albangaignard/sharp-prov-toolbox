package fr.cnrs.sharp.test;

/*
 * The MIT License
 *
 * Copyright 2017 Alban Gaignard <alban.gaignard@univ-nantes.fr>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Alban Gaignard <alban.gaignard@univ-nantes.fr>
 */
public class GenProvenanceForFile {

    public GenProvenanceForFile() {
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
    public void hello() throws FileNotFoundException, IOException {

        StopWatch sw = new StopWatch();
        sw.start();

        Path p = Paths.get("/Users/gaignard-a/Desktop/access.log-20150818");
        String label = p.getFileName().toString();
        System.out.println();

        FileInputStream fis = new FileInputStream(p.toFile());
        String sha512 = DigestUtils.sha512Hex(fis);
        System.out.println("");
        System.out.println(sha512);
        System.out.println("SHA512 calculated in " + sw.getTime() + " ms.");
        
        StringBuffer sb = new StringBuffer();
        sb.append("@base         <http://fr.symetric> .\n"
                + "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .\n"
                + "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n"
                + "@prefix sioc: <http://rdfs.org/sioc/ns#> .\n"
                + "@prefix prov: <http://www.w3.org/ns/prov#> .\n"
                + "@prefix sym:   <http://fr.symetric/vocab#> .\n"
                + "@prefix dcterms: <http://purl.org/dc/terms/> .\n"
                + "@prefix tavernaprov:  <http://ns.taverna.org.uk/2012/tavernaprov/> .\n"
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n");
        
        
        sb.append("<#" + UUID.randomUUID().toString() + ">\n"
                            + "\t a prov:Entity ;\n");
        sb.append("\t rdfs:label \""+label+"\"^^xsd:String ;\n");
        sb.append("\t tavernaprov:sha512 \""+sha512+"\"^^xsd:String .");
        
        System.out.println("");
        System.out.println("");
        System.out.println(sb.toString());

        fis.close();

    }
}
