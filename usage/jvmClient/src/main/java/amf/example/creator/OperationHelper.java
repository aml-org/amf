package amf.example.creator;

import amf.model.Operation;

/**
 * Helper class to populate a given operation and subclasses with example data.
 */
class OperationHelper {
    
    static void populateSearchAlbums(Operation searchAlbums){
        searchAlbums
                .withDescription("[Get Several Albums](https://developer.spotify.com/web-api/get-several-albums/)");
        searchAlbums.withRequest(RequestBuilder.albums());
        
        
    }
    
    static void populateGetAlbum(Operation getAlbum){
        getAlbum.withDescription(" [Get an Album](https://developer.spotify.com/web-api/get-albums/)")
                .withRequest(RequestBuilder.album());
    }
    
    static void populateGetUser(Operation getUser) {
        getUser.withDescription("[Get Current User's Profile](https://developer.spotify.com/web-api/get-current-users-profile/)");
    }
    
    static void populateListPlaylists(Operation listPlaylists) {
        listPlaylists.withDescription("[Get a List of Current User's Playlists](https://developer.spotify.com/web-api/get-a-list-of-current-users-playlists/)");
        ResponseHelper.populate200Playlists(listPlaylists.withResponse("200"));
    }
}
