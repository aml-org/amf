package amf.example.creator;

import amf.model.Parameter;

/**
 * Helper class to populate a given parameter with example data.
 */
class ParameterHelper {
    
    static void populateIds(Parameter ids){
        ids.withName("displayName")
                .withSchema("String")
                .withDescription("A comma-separated list of IDs")
                .withRequired(true);
    }
    
    static void populateMaker(Parameter ids){
        ids.withName("Market")
                .withSchema("String")
                .withDescription(" The market (an ISO 3166-1 alpha-2 country code)")
                .withRequired(false);
    }
    
    static void populateIdAlbum(Parameter id){
        id.withName("Spotify Album ID")
                .withSchema("string")
                .withDescription("The Spotify ID for the albums");
    }
}
