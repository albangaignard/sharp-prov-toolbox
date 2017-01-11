package fr.cnrs.sharp.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import fr.cnrs.sharp.reasoning.Harmonization;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class ProvStoreExpeTest {

    private static final Logger logger = Logger.getLogger(ProvStoreExpeTest.class);

    public ProvStoreExpeTest() {
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
    public void helloProvStore() throws URISyntaxException {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        WebResource service = client.resource(new URI("http://provenance.ecs.soton.ac.uk/store"));

        ClientResponse responseHist = service.path("/api/v0/documents").queryParam("limit", "10").queryParam("created_at__range", "2016-01-01,2016-12-01").accept("application/json").type("application/json").get(ClientResponse.class);
        String rH = responseHist.getEntity(String.class);

        JsonArray arrayDocs = new JsonParser().parse(rH).getAsJsonObject().get("objects").getAsJsonArray();
        for (JsonElement eH : arrayDocs) {
            String id = eH.getAsJsonObject().get("id").getAsString();
            System.out.println(id);
        }
        Assert.assertEquals(10, arrayDocs.size());
    }

    @Test
    @Ignore
    public void wildProvExpe() throws IOException {
        String prefix = "/Users/gaignard-a/Documents/Publis/eswc-2017/provenance-reasoning-mine/experiments/sample-prov-store-docs";

//        String[] ids = {"113207","113206","113263"};
//        for (String id : ids) {
//            Path p = Paths.get(prefix + "/"+id+".ttl"); 
//            System.out.println("Working on "+id);
//            harmonizeProvNoLog(p);
//        }
//        Path p = Paths.get(prefix + "/113207.ttl"); //small
//        Path p = Paths.get(prefix + "/113206.ttl"); //medium
        Path p = Paths.get(prefix + "/113263.ttl"); //large
        Harmonization.harmonizeProv(p.toFile());
    }
}
