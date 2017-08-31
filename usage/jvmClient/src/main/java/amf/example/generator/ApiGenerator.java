package amf.example.generator;

import amf.client.AmfGenerator;
import amf.client.OasGenerator;
import amf.client.RamlGenerator;
import amf.model.Document;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Client generation class to show how to use the generation amf api. Public methods and exceptions are examples to be used as guidelines.
 */
public class ApiGenerator {
    
    private final Document document;
    
    public ApiGenerator(Document document) {
        this.document = document;
    }
    
    public String generateOasString() throws GeneratorException {
        return handleFuture(new OasGenerator().generateString(document));
    }
    
    public String generateRamlString() throws GeneratorException {
        return handleFuture(new RamlGenerator().generateString(document));
    }
    
    public String generateAmfString() throws GeneratorException {
        return handleFuture(new AmfGenerator().generateString(document));
    }
    
    public String generateOasFile(String path) throws GeneratorException {
        return handleFuture(new OasGenerator().generateFile(document,new File(filePrefix+path)));
    }
    
    public String generateRamlFile(String path) throws GeneratorException {
        return handleFuture(new RamlGenerator().generateFile(document,new File(filePrefix+path)));
    }
    
    public String generateAmfFile(String path) throws GeneratorException {
        return handleFuture(new AmfGenerator().generateFile(document,new File(filePrefix+path)));
    }
    
    private String handleFuture(CompletableFuture<String> f) throws GeneratorException {
        
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new GeneratorException("An error happend while dumping the document. Message: " + e.getMessage(), e);
        }
    }
    
    private static final String filePrefix = "file://";
}
