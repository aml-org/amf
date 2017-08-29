package amf.example.creator;

import amf.model.Response;

/**
 * Helper class to populate a given operation response and subclasses with example data.
 */
class ResponseHelper {
    
    static void populate200Playlists(Response response){
        PayloadHelper.populate200Playlists(response.withPayload());
    }
    
    
}
