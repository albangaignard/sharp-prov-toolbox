/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.sharp.reasoning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
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
import org.apache.log4j.Logger;

/**
 *
 * @author Alban Gaignard <alban.gaignard@univ-nantes.fr>
 */
public class Interlinking {

    private static final Logger logger = Logger.getLogger(Harmonization.class);

    /**
     * Generates a Jena Model with an SHA-512 fingerprint of a file content.
     * @param inputRawFile the file to be fingerprinted
     * @return a Jena Model with an SHA-512, based on PROV and tavernaprov vocabularies.
     * @throws IOException
     */
    public static Model fingerprint(Path inputRawFile) throws IOException {
        StopWatch sw = new StopWatch();
        sw.start();

        Model data = ModelFactory.createDefaultModel();

        Path p = inputRawFile;
        String label = p.getFileName().toString();
        System.out.println();

        FileInputStream fis = new FileInputStream(p.toFile());
        String sha512 = DigestUtils.sha512Hex(fis);

        StringBuffer sb = new StringBuffer();
        sb.append("@base         <http://fr.symetric> .\n"
                + "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .\n"
                + "@prefix prov: <http://www.w3.org/ns/prov#> .\n"
                + "@prefix tavernaprov:  <http://ns.taverna.org.uk/2012/tavernaprov/> .\n"
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n");

        sb.append("<#" + UUID.randomUUID().toString() + ">\n"
                + "\t a prov:Entity ;\n");
        sb.append("\t rdfs:label \"" + label + "\"^^xsd:String ;\n");
        sb.append("\t tavernaprov:sha512 \"" + sha512 + "\"^^xsd:String .");

        RDFDataMgr.read(data, new StringReader(sb.toString()), null, Lang.TTL);

        fis.close();
        return data;
    }

    /**
     * Generates a Jena Model with owl:sameAs annotations for the same files, based on SHA-512 fingerprints. 
     * @param m the model to be analysed. 
     * @return a Jena Model with owl:sameAs annotations for the same files, based on SHA-512 fingerprints. 
     */
    public static Model generateSameAs(Model m) {
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
                + "    FILTER(( ?x_sha512 = ?y_sha512 ) && (?x != ?y)) .\n"
                + "}";
        
        Query query = QueryFactory.create(constructSameAs);
        QueryExecution queryExec = QueryExecutionFactory.create(query, m);
        Model sameAsModel = queryExec.execConstruct();
        sameAsModel.add(m);
        queryExec.close();

        return sameAsModel;
    }
}
