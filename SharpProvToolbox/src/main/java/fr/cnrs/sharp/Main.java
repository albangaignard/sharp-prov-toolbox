package fr.cnrs.sharp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.cnrs.sharp.reasoning.Harmonization;
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
        
        Option inFileOpt = OptionBuilder.withArgName("input_file_1> ... <input_file_n")
                .withValueSeparator(' ')
                .hasArgs()
                .withLongOpt("input_files")
                .withDescription("The list of PROV input files, in RDF Turtle.")
                .create("i");
        
        options.addOption(inFileOpt);
        options.addOption(versionOpt);
        options.addOption(helpOpt);
        
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
                } catch (IOException ex) {
                    logger.error("Impossible to write the harmonized provenance file.");
                    System.exit(1);
                }
                
            }
            
        } catch (ParseException ex) {
            logger.error("Error while parsing command line arguments. Please check the following help:");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SharpToolBox", header, options, footer, true);
            System.exit(1);
        }
    }
}
