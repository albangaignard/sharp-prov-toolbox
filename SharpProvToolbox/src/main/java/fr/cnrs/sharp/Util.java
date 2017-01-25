/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.sharp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

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

    public static String getD3Json(Model m) {
        StringBuilder bb = new StringBuilder();

        bb.append("{");
        bb.append("\n");
        bb.append(" \"nodes\" : [ ");
        bb.append("\n");
        bb.append(Util.d3Nodes(m));
        bb.append("] , \n");

        bb.append(" \"links\" : [ ");
        bb.append("\n");
        bb.append(Util.d3Edges(m));
        bb.append("] ");
        bb.append("\n");
        bb.append("}");
        return bb.toString();
    }

    private static String getLabel(RDFNode n) {
        String label = "none";
        if (n.isAnon()) {
            label = n.asNode().getBlankNodeLabel().toString();
        } else if (n.isLiteral()) {
            label = n.asLiteral().getValue().toString();
        } else if (n.isResource()) {
            label = n.asResource().getURI().toString();
        } else {
            label = n.toString();
        }
        return Util.jsonEscapes(label);
    }
    
    private static String jsonEscapes(String str) {
//        System.out.println("ESCAPING "+str);
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i)) {
                case 0:
                    continue;
                case '\b':
                    escaped.append("\\\\b");
                    continue;
                case '\t':
                    escaped.append("\\\\t");
                    continue;
                case '\n':
                    escaped.append("\\\\n");
                    continue;
                case '\f':
                    escaped.append("\\\\f");
                    continue;
                case '\r':
                    escaped.append("\\\\r");
                    continue;
                case '\"':
                    escaped.append("\\\\\"");
                    continue;
                case '\\':
                    escaped.append("\\\\\\");
                    continue;
                default:
                    escaped.append(str.charAt(i));
                    continue;
            }
        }
        return escaped.toString();
    }

    private static String d3Nodes(Model m) {
        HashMap<String, Integer> nodes = new HashMap();

        StringBuilder sb = new StringBuilder();
        StmtIterator it = m.listStatements();
        while (it.hasNext()) {
            Statement st = it.next();

            RDFNode s = st.getSubject();
            String label = Util.getLabel(s);
            if (s.isAnon()) {
                if (!nodes.containsKey(label)) {
                    nodes.put(label, 0);
                }
            } else if (s.isLiteral()) {
                if (!nodes.containsKey(label)) {
                    nodes.put(label, 1);
                }
            } else if (s.isResource()) {
                if (!nodes.containsKey(label)) {
                    nodes.put(label, 2);
                }
            }

            RDFNode o = st.getObject();
            label = Util.getLabel(o);
            if (o.isAnon()) {
                if (!nodes.containsKey(label)) {
                    nodes.put(label, 0);
                }
            } else if (o.isLiteral()) {
                if (!nodes.containsKey(label)) {
                    nodes.put(label, 1);
                }
            } else if (o.isResource()) {
                if (!nodes.containsKey(label)) {
                    nodes.put(label, 2);
                }
            }
        }

        for (Entry e : nodes.entrySet()) {
            sb.append("\t { \"id\" : \"" + e.getKey().toString() + "\", \"group\" : \"" + e.getValue().toString() + "\" },\n");
        }

        return sb.toString();
    }

    private static String d3Edges(Model m) {
        StringBuilder sb = new StringBuilder();
        StmtIterator it = m.listStatements();
        while (it.hasNext()) {
            Statement st = it.next();
            sb.append("\t {");
            sb.append("\"source\" : \"" + Util.getLabel(st.getSubject()) + "\"");
            sb.append(", ");
            sb.append("\"target\" : \"" + Util.getLabel(st.getObject()) + "\"");
            sb.append(", ");
            sb.append("\"label\" : \"" + st.getPredicate().toString() + "\"");
            sb.append("} , \n ");
        }

        if (sb.toString().contains(",")) {
            sb.deleteCharAt(sb.lastIndexOf(","));
        }

        return sb.toString();
    }
    
    public static String genHtmlViz(String jsonData) {
        InputStream is = Util.class.getClassLoader().getResourceAsStream("prov-vis-template.html");
        String text = null;
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }

        String out = text.replaceAll(Pattern.quote("**jsonData**"), jsonData);
        return out;
    }
    
    public static Path writeHtmlViz(Model m) throws IOException {
        
        String jsonData = Util.getD3Json(m);
//        Gson gson = new Gson();
//        Object ob = gson.fromJson(jsonData, Object.class);
//        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(ob));
        String htmlOut = Util.genHtmlViz(jsonData);
        
        Path pathHtml = Files.createTempFile("provenanceDisplay-", ".html");
        Files.write(pathHtml, htmlOut.getBytes(), StandardOpenOption.WRITE);
        System.out.println("HTML visualization written in " + pathHtml.toString());
        return pathHtml;
    }
}
