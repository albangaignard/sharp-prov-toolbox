/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.sharp.reasoning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.log4j.Logger;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class Unification {

    private static final Logger logger = Logger.getLogger(Unification.class);
    
    public static int realSize(Model m) {
        return m.listStatements().toList().size();
    }
    
    public static int countBN(Model m) {
        Set<RDFNode> set = new HashSet<RDFNode>();
        ResIterator resIt = m.listSubjects();
        while (resIt.hasNext()) {
            Resource r = resIt.next();
            if (r.isAnon()) {
                set.add(r);
            }
        }
        NodeIterator nodeIt = m.listObjects();
        while (nodeIt.hasNext()) {
            RDFNode n = nodeIt.next();
            if (n.isAnon()) {
                set.add(n);
            }
        }
        return set.size();
    }

    public static List<Pair<RDFNode, RDFNode>> selectSubstitutions(Model m) {
        // UNIFICATION : 1. finding substitution of existential variables 
        String querySubstitutionFinding = "SELECT distinct ?blank_node ?node  WHERE { \n"
                + "    { \n"
                + "        ?i ?p ?blank_node .\n"
                + "        FILTER (isBlank(?blank_node)) .\n"
                + "\n"
                + "        ?i ?p ?node .\n"
                + "        FILTER ((?node != ?blank_node) && (! isBlank(?node))) .\n"
                + "    } UNION { \n"
                + "        ?blank_node ?q ?j .\n"
                + "        FILTER (isBlank(?blank_node)) .\n"
                + "\n"
                + "        ?node ?q ?j .\n"
                + "        FILTER ((?node != ?blank_node) && (! isBlank(?node))) .\n"
                + "    }\n"
                + "}";

        Query query = QueryFactory.create(querySubstitutionFinding);
        QueryExecution qexec = QueryExecutionFactory.create(query, m);
        ResultSet results = qexec.execSelect();

        List<Pair<RDFNode, RDFNode>> toBeMerged = new ArrayList<>();
        while (results.hasNext()) {
            QuerySolution qs = results.nextSolution();
            RDFNode blankN = qs.get("?blank_node");
            RDFNode node = qs.get("?node");
            toBeMerged.add(new ImmutablePair(blankN, node));
        }
//        ResultSetFormatter.out(System.out, results, query);
        qexec.close();
        return toBeMerged;
    }

    public static int countBNasSubject(Model m) {
        int count = 0;
        for (Resource r : m.listSubjects().toList()) {
            if (r.isAnon()) {
                count++;
            }
        }
        return count;
    }

    public static int countBNasObject(Model m) {
        int count = 0;
        for (RDFNode n : m.listObjects().toList()) {
            if (n.isAnon()) {
                count++;
            }
        }
        return count;
    }

    /**
     *
     * @param source an RDFNode, i.e. either an URI, a blank node, or a literal,
     * to be merged with the target node.
     * @param target a Resource, i.e. an URI or a blank node as a result of the
     * merge operation.
     */
    public static void mergeNodes(RDFNode source, Resource target) {
        Model model = target.getModel();
//        logger.debug(model.size());

        //merging outgoing edges
        Selector sOut = new SimpleSelector((Resource) source, null, (RDFNode) null);
        for (Statement st : model.listStatements(sOut).toList()) {
            Statement newSt = model.createStatement(target, st.getPredicate(), st.getObject());
            model = model.add(newSt);
//            logger.debug("CREATED outgoing " + newSt);
            model = model.remove(st);
//            logger.debug("REMOVED " + st);
        }

        //merging incoming edges
        Selector sIn = new SimpleSelector(null, null, source);
        for (Statement st : model.listStatements(sIn).toList()) {
            Statement newSt = model.createStatement(st.getSubject(), st.getPredicate(), target);
            model = model.add(newSt);
//            logger.debug("CREATED incoming " + newSt);
            model = model.remove(st);
//            logger.debug("REMOVED " + st);
        }
    }
}
