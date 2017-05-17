package fr.cnrs.sharp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.cnrs.sharp.reasoning.Harmonization;
import fr.cnrs.sharp.reasoning.Interlinking;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String args[]) {
        Options options = new Options();

        Option versionOpt = new Option("v", "version", false, "print the version information and exit");
        Option helpOpt = new Option("h", "help", false, "print the help");

        Option inProvFileOpt = OptionBuilder.withArgName("input_PROV_file_1> ... <input_PROV_file_n")
                .withLongOpt("input_PROV_files")
                .withDescription("The list of PROV input files, in RDF Turtle.")
                .hasArgs()
                .create("i");

        Option inRawFileOpt = OptionBuilder.withArgName("input_raw_file_1> ... <input_raw_file_n")
                .withLongOpt("input_raw_files")
                .withDescription("The list of raw files to be fingerprinted and possibly interlinked with owl:sameAs.")
                .hasArgs()
                .create("ri");

        Option summaryOpt = OptionBuilder.withArgName("summary")
                .withLongOpt("summary")
                .withDescription("Materialization of wasInfluencedBy relations.")
                .create("s");

        options.addOption(inProvFileOpt);
        options.addOption(inRawFileOpt);
        options.addOption(versionOpt);
        options.addOption(helpOpt);
        options.addOption(summaryOpt);

        String header = "SharpTB is a tool to maturate provenance based on PROV inferences";
        String footer = "\nPlease report any issue to alban.gaignard@univ-nantes.fr";

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("SharpTB", header, options, footer, true);
                System.exit(0);
            }

            if (cmd.hasOption("v")) {
                logger.info("SharpTB version 0.1.0");
                System.exit(0);
            }

            if (cmd.hasOption("ri")) {
                String[] inFiles = cmd.getOptionValues("ri");
                Model model = ModelFactory.createDefaultModel();
                for (String inFile : inFiles) {
                    Path p = Paths.get(inFile);
                    if (!p.toFile().isFile()) {
                        logger.error("Cannot find file " + inFile);
                        System.exit(1);
                    } else {
                        //1. fingerprint
                        try {
                            model.add(Interlinking.fingerprint(p));
                        } catch (IOException e) {
                            logger.error("Cannot fingerprint file " + inFile);
                        }
                    }
                }
                //2. genSameAs
                Model sameAs = Interlinking.generateSameAs(model);
                sameAs.write(System.out, "TTL");
            }

            if (cmd.hasOption("i")) {
                String[] inFiles = cmd.getOptionValues("i");
                Model data = ModelFactory.createDefaultModel();
                for (String inFile : inFiles) {
                    Path p = Paths.get(inFile);
                    if (!p.toFile().isFile()) {
                        logger.error("Cannot find file " + inFile);
                        System.exit(1);
                    } else {
                        RDFDataMgr.read(data, inFile, Lang.TTL);
                    }
                }
                Model res = Harmonization.harmonizeProv(data);

                try {
                    Path pathInfProv = Files.createTempFile("PROV-inf-tgd-egd-", ".ttl");
                    res.write(new FileWriter(pathInfProv.toFile()), "TTL");
                    System.out.println("Harmonized PROV written to file " + pathInfProv.toString());

                    //if the summary option is activated, then save the subgraph and generate a visualization
                    if (cmd.hasOption("s")) {

                        String queryInfluence
                                = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
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
                        Util.writeHtmlViz(summary);
                    }

                } catch (IOException ex) {
                    logger.error("Impossible to write the harmonized provenance file.");
                    System.exit(1);
                }
            } else {
//                logger.info("Please fill the -i input parameter.");
//                HelpFormatter formatter = new HelpFormatter();
//                formatter.printHelp("SharpTB", header, options, footer, true);
//                System.exit(0);
            }

        } catch (ParseException ex) {
            logger.error("Error while parsing command line arguments. Please check the following help:");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SharpToolBox", header, options, footer, true);
            System.exit(1);
        }
    }
}
