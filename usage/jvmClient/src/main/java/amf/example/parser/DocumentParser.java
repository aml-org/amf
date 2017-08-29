package amf.example.parser;

import amf.client.AmfParser;
import amf.client.OasParser;
import amf.client.RamlParser;
import amf.model.BaseUnit;
import amf.model.Document;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * Client parser class for show how to use the parse of amf api. Public methods and exceptions are examples of how to, and does not belong to the api.
 */
public class DocumentParser {
    
    
    public Document parseRamlFile(String path) throws ParserException {
        return handleFuture(new RamlParser().parseFileAsync(filePrefix+path));
    }
    
    public Document parseOasFile(String path) throws ParserException {
        return handleFuture(new OasParser().parseFileAsync(filePrefix+path));
    }
    
    public Document parseAmfFile(String path) throws ParserException {
        return handleFuture(new AmfParser().parseFileAsync(filePrefix+path));
    }
    
    public Document parseExample() throws ParserException {
        return handleFuture(new RamlParser().parseStringAsync(spotifyRamlApi));
    }
    
    public Document parseRamlString(String api) throws ParserException {
        return handleFuture(new RamlParser().parseStringAsync(api));
    }
    
    public Document parseOasString(String api) throws ParserException {
        return handleFuture(new OasParser().parseStringAsync(api));
    }
    
    public Document parseAmfString(String api) throws ParserException {
        return handleFuture(new AmfParser().parseStringAsync(api));
    }
    
    private Document handleFuture(CompletableFuture<BaseUnit> f) throws ParserException {
        try {
            return (Document)f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ParserException("An error happend while parsing the api. Message: " + e.getMessage(), e);
        }
    }
    
    private static final String filePrefix = "file://";
    
    private static final String spotifyRamlApi = "#%RAML 1.0\n" +
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

