package amf.test;
import amf.client.*;
import amf.model.BaseUnit;
import amf.model.CreativeWork;
import amf.model.Document;
import amf.model.WebApi;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestMainClass {
    
    
    
    private static final String basePath= "/Users/hernan.najles/mulesoft/amf/usage/jvmClient/src/main/resources/output/";
    
    private static void purgeDirectory(){
        File file = new File(basePath);
        if(file.isDirectory()) {
            for (File f : file.listFiles())
                f.delete();
        }
    }
    
    public static void main(String... args){
        purgeDirectory();
        try {
            System.out.println("\n-------------------------------------------------------------------------------");
            System.out.println("Test ASync");
            WebApi fromScratch = webApiFromScratch();
            WebApi fromStream = baseUnitFromStream();
            
            System.out.println("FromScratch to string: "+dumpApiToStream(fromScratch));
            System.out.println("FromStream to string: "+dumpApiToStream(fromStream));
            
            
            dumpApiToFile(fromScratch,"fromScratch");
            dumpApiToFile(fromStream,"fromStream");
            
            /* uncomment to use handled method. Beware that methods are async, so thread sleep must be called*/
            handledMode();
        
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    
    private static void handledMode(){
        new JvmClient().generateFromStream(completeRamlApi, HintHelper.ramlYamlHint(), new Handler<BaseUnit>() {
            @Override
            public void error(Throwable exception) {
                System.out.println("Error reading stream: "+exception);
            }
            
            @Override
            public void success(BaseUnit document) {
                System.out.println("Base unit generated: "+ ((Document)document).encodes());
                new JvmGenerator().generateToString(document, VendorHelper.oas(), new StringHandler() {
                    @Override
                    public void error(Throwable exception) {
                        System.out.println("Error reading stream: "+exception);
                    }
                    
                    @Override
                    public void success(String generation) {
                        System.out.println("String generated from base unit: "+generation);
                    }
                });
            }
        });
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private static WebApi webApiFromScratch(){
        System.out.println("write here your code for test the web api :)");
        List<String> list = Arrays.asList("one","two");
        
        
        WebApi api = new WebApi();
        api.withSchemes(list);
        api.withDescription("operation description");
        api.withName("operation description");
        
        api.withEndPoint("endpoint")
                .withDescription("endpoint description")
                .withName("endpoint anme")
                .withOperation("get")
                .withDocumentation(new CreativeWork().withDescription("o1 description").withUrl("o1 cw url"))
                .withName("operation name");
        
        return api;
    }
    
    private static WebApi baseUnitFromStream() throws InterruptedException, ExecutionException {
        CompletableFuture<BaseUnit> future = new JvmClient().generateAsyncFromStream(completeRamlApi, HintHelper.ramlYamlHint());
        final BaseUnit baseUnit = future.get();
        return ((Document)baseUnit).encodes();
    }
    
    private static String dumpApiToStream(@NotNull WebApi webApi) throws ExecutionException, InterruptedException {
        Document build = new Document(webApi);
        CompletableFuture<String> stringFuture = new JvmGenerator().generateToStringAsync(build, VendorHelper.raml());
        return stringFuture.get();
    }
    
    private static void dumpApiToFile(@NotNull WebApi webApi, String name) throws ExecutionException, InterruptedException {
        Document build = new Document(webApi);
        CompletableFuture<String> stringFuture = new JvmGenerator().
                generateToFileAsync(build, "file://"+basePath+ name +".json",VendorHelper.raml());
        stringFuture.get();
    }
    
    
    private static final String completeRamlApi = "#%RAML 1.0\n" +
            "title: test title\n" +
            "description: test description\n" +
            "(termsOfService): terms of service\n" +
            "version: 1.1\n" +
            "(license):\n" +
            "  url: licenseUrl\n" +
            "  name: licenseName\n" +
            "baseUri: http://api.example.com/path\n" +
            "mediaType:\n" +
            "  - application/yaml\n" +
            "protocols:\n" +
            "  - http\n" +
            "  - https\n" +
            "(contact):\n" +
            "  url: contactUrl\n" +
            "  name: contactName\n" +
            "  email: contactEmail\n" +
            "(externalDocs):\n" +
            "  url: externalDocsUrl\n" +
            "  description: externalDocsDescription";
}