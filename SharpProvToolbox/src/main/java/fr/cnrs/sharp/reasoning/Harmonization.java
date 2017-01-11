/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.sharp.reasoning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
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

/**
 *
 * @author Alban Gaignard <alban.gaignard@univ-nantes.fr>
 */
public class Harmonization {

    private static final Logger logger = Logger.getLogger(Harmonization.class);

    public static File harmonizeProv(File inputProv) throws IOException {
        Model inputGraph = FileManager.get().loadModel(inputProv.getAbsolutePath());
        Model harmonizedProv = harmonizeProv(inputGraph);
        
        Path pathInfProv = Files.createTempFile("PROV-inf-tgd-egd-", ".ttl");
        harmonizedProv.write(new FileWriter(pathInfProv.toFile()), "TTL");
        logger.info("PROV inferences file written to " + pathInfProv.toString());
        return pathInfProv.toFile();
    }
    
    public static Model harmonizeProv(Model inputProvGraph) throws IOException {
        logger.info("Asserted graph : Graph size / BNodes : " + inputProvGraph.size() + "/" + Unification.countBN(inputProvGraph));
//        Util.dumpPredStats(inputProvGraph);

        /// STEP 1 : OWL sameAs inferences
        Model schema = ModelFactory.createDefaultModel();
        RDFDataMgr.read(schema, Harmonization.class.getClassLoader().getResourceAsStream("provo.ttl"), Lang.TURTLE);
        Reasoner owlReasoner = ReasonerRegistry.getOWLMiniReasoner();
        owlReasoner = owlReasoner.bindSchema(schema);
        InfModel owlModel = ModelFactory.createInfModel(owlReasoner, inputProvGraph);
//        logger.info("OWL entail : Graph size / BNodes : " + owlModel.size() + "/" + Unification.countBN(owlModel));
//        Util.dumpPredStats(owlModel);

        /// STEP 2.1 : PROV inferences TGD == saturation
        List rules = Rule.rulesFromURL("provRules_all.jena");
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        InfModel inferredModel = ModelFactory.createInfModel(reasoner, owlModel);

        /// STEP 2.2 : PROV inferences EGD == unification
        Model harmonizedModel = ModelFactory.createDefaultModel().add(inferredModel);

        int nbSubstitution = 1;
        while (nbSubstitution > 0) {
            // UNIFICATION : 1. finding substitution of existential variables 
            List<Pair<RDFNode, RDFNode>> toBeMerged = Unification.selectSubstitutions(harmonizedModel);
            nbSubstitution = toBeMerged.size();
            if (toBeMerged.size() > 0) {
                // UNIFICATION : 2. effectively replacing blank nodes by matching nodes
                for (Pair<RDFNode, RDFNode> p : toBeMerged) {
                    Unification.mergeNodes(p.getLeft(), p.getRight().asResource());
                }
                nbSubstitution = Unification.selectSubstitutions(harmonizedModel).size();
            }
        }

        logger.info("OWL+TGD+EGD : Graph size / BNodes : " + harmonizedModel.size() + "/" + Unification.countBN(harmonizedModel));
//        Util.dumpPredStats(harmonizedModel);
        return harmonizedModel;
    }
}
