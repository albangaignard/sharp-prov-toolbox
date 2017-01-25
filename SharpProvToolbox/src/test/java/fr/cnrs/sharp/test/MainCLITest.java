/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.sharp.test;

import fr.cnrs.sharp.Main;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
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
public class MainCLITest {

    public MainCLITest() {
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

    @Test
    public void Main1() throws IOException {
        InputStream is = MainCLITest.class.getClassLoader().getResourceAsStream("galaxy.prov.ttl");
        Path p = Files.createTempFile("test-prov", ".ttl");
        FileUtils.copyInputStreamToFile(is, p.toFile());
        System.out.println("Galaxy PROV written to " + p.toString());

        String[] params = {"-i", p.toString()};

        StopWatch sw = new StopWatch();
        sw.start();
        Main.main(params);
        sw.stop();
        System.out.println("DONE in " + sw.getTime() + " ms");

    }

    @Test
    public void Main2() throws IOException {
        InputStream is = MainCLITest.class.getClassLoader().getResourceAsStream("galaxy.prov.ttl");
        Path p = Files.createTempFile("test-prov", ".ttl");
        FileUtils.copyInputStreamToFile(is, p.toFile());
        System.out.println("Galaxy PROV written to " + p.toString());

        String[] params = {"-i", p.toString(), "-s"};

        StopWatch sw = new StopWatch();
        sw.start();
        Main.main(params);
        sw.stop();
        System.out.println("DONE in " + sw.getTime() + " ms");

    }
}
