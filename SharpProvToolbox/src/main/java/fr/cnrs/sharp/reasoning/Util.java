/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.sharp.reasoning;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

/**
 *
 * @author Alban Gaignard <alban.gaignard@univ-nantes.fr>
 */
public class Util {
    
    public static void dumpPredStats(Model m) {
        String topPredicatesQuery = "SELECT  ?p (COUNT(?s) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY DESC(?count) LIMIT 10";
        Query query = QueryFactory.create(topPredicatesQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, m);
        ResultSet results = qexec.execSelect();
        System.out.println(ResultSetFormatter.asText(results));
    }
    
    public static void selectBlankNodes(Model m) {
        String selectBNQuery = "SELECT * WHERE { ?s ?p ?o FILTER(isBlank(?s) || isBlank(?o))}";
        Query query = QueryFactory.create(selectBNQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, m);
        ResultSet results = qexec.execSelect();
        System.out.println(ResultSetFormatter.asText(results));
    }
    
    public static void describeBlankNodes(Model m) {
        String selectBNQuery = "DESCRIBE ?s ?o WHERE { ?s ?p ?o FILTER(isBlank(?s) || isBlank(?o))}";
        Query query = QueryFactory.create(selectBNQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, m);
        Model results = qexec.execDescribe();
        results.write(System.out, "TTL");
    }
}
