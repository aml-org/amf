package amf.example.creator;

import amf.model.Payload;

/**
 * Helper class to populate a given payload with example data.
 */
class PayloadHelper {
    
    static void populate200Playlists(Payload payload){
        payload.withMediaType("application/json")
                .withSchema("string");
    }
}
