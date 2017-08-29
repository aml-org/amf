package amf.example.creator;

import amf.model.Request;

/**
 * Builder class to create a request and populate her subclasses with example data.
 */
class RequestBuilder {
    
    static Request albums(){
        final Request request = new Request();
        ParameterHelper.populateIds(request.withQueryParameter("ids"));
        ParameterHelper.populateMaker(request.withQueryParameter("market"));
        
        return request;
    }
    
    static Request album(){
        final Request request = new Request();
        ParameterHelper.populateMaker(request.withQueryParameter("market"));
    
        return request;
    }
    
    
}
