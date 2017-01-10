package fr.cnrs.sharp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

/**
 *
 * @author Alban Gaignard <alban.gaignard@univ-nantes.fr>
 */
public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    private static String gKey;
    private static String gHist;
    private static String gUrl;
    private static boolean isFull = false;

    public static void main(String args[]) {
        Options options = new Options();
        Option listGHistOpt = new Option("l", "listGalaxyHistories", false, "list all available Galaxy histories");
        Option apiKeyOpt = new Option("k", "galaxyApiKey", true, "the Galaxy API Key used as credential");
        Option gUrlOpt = new Option("u", "galaxyUrl", true, "the Galaxy server URL");
        Option gHistIDOpt = new Option("hi", "galaxyHistoryID", true, "the Galaxy history ID used to extract PROV RDF statements");
        Option fullOpt = new Option("f", "full_prov", false, "produces a full RDF PROV output");
        Option versionOpt = new Option("v", "version", false, "print the version information and exit");
        Option helpOpt = new Option("h", "help", false, "print the help");
        options.addOption(listGHistOpt);
        options.addOption(apiKeyOpt);
        options.addOption(gHistIDOpt);
        options.addOption(gUrlOpt);
        options.addOption(fullOpt);
        options.addOption(versionOpt);
        options.addOption(helpOpt);

        String header = "Ga2Prov is a tool to extract provenance information from Galaxy workspaces through the PROV-O ontology and a corresponding graphical representation.";
        String footer = "\nPlease report any issue to alban.gaignard@cnrs.fr";

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("SharpToolBox", header, options, footer, true);
                System.exit(0);
            }

            if (cmd.hasOption("v")) {
                logger.info("SharpToolBox version 0.1.0");
                System.exit(0);
            }

            if (cmd.hasOption("f")) {
                Main.isFull = true;
                logger.debug("full prov");
            } else {
                logger.debug("partial prov");
            }

            if (cmd.hasOption("k")) {
                Main.gKey = cmd.getOptionValue("k");
            } else {
                logger.warn("Please specify your Galaxy API key !");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("ga2prov", header, options, footer, true);
                System.exit(0);
            }

            if (cmd.hasOption("u")) {
                Main.gUrl = cmd.getOptionValue("u");
            } else {
                logger.warn("Please specify your Galaxy server URL to connect to !");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("SharpToolBox", header, options, footer, true);
                System.exit(0);
            }
        } catch (ParseException ex) {
            logger.error("Error while parsing command line arguments. Please check the following help:");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SharpToolBox", header, options, footer, true);
            System.exit(1);
        }
    }
}
