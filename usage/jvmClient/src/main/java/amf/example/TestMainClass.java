package amf.example;
import amf.example.creator.DocumentCreator;
import amf.example.generator.ApiGenerator;
import amf.example.generator.GeneratorException;
import amf.example.parser.DocumentParser;
import amf.example.parser.ParserException;
import amf.model.CreativeWork;
import amf.model.Document;

import java.io.File;
import java.io.IOException;

public class TestMainClass {
    
    
    
    private static final String basePath= "/resources/";
    
    private static String workingDirectory = basePath;
    
    private static void purgeDirectory(){
        try {
            workingDirectory = new File(".").getCanonicalPath() +  "/src/main/resources/";
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        File file = new File(workingDirectory+"output/");
        if(file.isDirectory()) {
            //noinspection ConstantConditions
            for (File f : file.listFiles())
                //noinspection ResultOfMethodCallIgnored
                f.delete();
        }
    }
    
    /* exec this method to run the examples*/
    public static void main(String... args){
        purgeDirectory();
        
        System.out.println("\n-------------------------------------------------------------------------------");
        System.out.println("Test ASync");
        
        /*new Document api from scratch*/
        Document fromScratch = DocumentCreator.spotifyApiDocument();
    
    
        /* Parse internal raml api example*/
        Document parsedApi = null;
        try {
            parsedApi = new DocumentParser().parseExample();
        } catch (ParserException e) {
            e.printStackTrace();
        }
    
        /* Parse baking jsonld api from examples directory */
        Document parsedAmfApi = null;
        try {
            parsedAmfApi = new DocumentParser().parseAmfFile(workingDirectory+"examples/banking-api.jsonld");
        } catch (ParserException e) {
            e.printStackTrace();
        }
    
        Document parsedOasApi = null;
        try {
            parsedOasApi = new DocumentParser().parseOasFile(workingDirectory+"examples/banking-api.json");
        } catch (ParserException e) {
            e.printStackTrace();
        }
        
        /* Generate oas api string from document */
        if(parsedApi!=null){
            try {
                String s = new ApiGenerator(parsedApi).generateOasString();
                System.out.println("Document dumpped: "+ s);
            } catch (GeneratorException e) {
                e.printStackTrace();
            }
        }
    
        /* Generate oas api file from new in memory api*/
        try {
            String s = new ApiGenerator(fromScratch).generateOasFile(workingDirectory+"output/generatedFile.json");
            System.out.println("Document dumpped: "+ s);
        } catch (GeneratorException e) {
            e.printStackTrace();
        }
        
        
        /* mutate and existing document*/
        if(parsedOasApi!=null){
    
            parsedOasApi.encodes()
                    .withDocumentation(new CreativeWork()
                                                .withUrl("http://example.com/baking.raml")
                                                .withDescription("ACME Banking HTTP API "));
    
            parsedOasApi.encodes()
                    .withEndPoint("/customer/accounts").withName("Customer Accounts");
    
            parsedOasApi.encodes()
                    .endPoints().get(0)
                    .withName("New Customer Resource")
                    .withOperation("get")
                        .withDescription("Gets a customer");
    
            /* Generate raml api file from mutated api*/
            try {
                String s = new ApiGenerator(parsedOasApi).generateRamlString();
                System.out.println("Document mutated dumpp: "+ s);
            } catch (GeneratorException e) {
                e.printStackTrace();
            }
        }
    }
}