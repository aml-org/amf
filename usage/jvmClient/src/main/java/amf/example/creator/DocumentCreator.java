package amf.example.creator;

import amf.model.*;

import java.util.Collections;

/**
 * Client class for creating a document with a web api from scratch. Use this code as example for build apis programmatically
 */
public class DocumentCreator {
    
    
    
    public static Document spotifyApiDocument(){
        return new Document(spotifyApi());
    }
    
    private static WebApi spotifyApi(){
        
        WebApi api = new WebApi()
                        .withName("Spotify Web API")
                        .withVersion("v1")
                         .withBasePath("{version}")
                .withContentType(Collections.singletonList("application/json"))
                .withAccepts(Collections.singletonList("application/json"));
        
        EndPointHelper.populateAlbums(api.withEndPoint("/albums"));
        EndPointHelper.populateAlbumId(api.withEndPoint("/albums/{id}"));
        EndPointHelper.populateMe(api.withEndPoint("/me"));
        EndPointHelper.populatePaylists(api.withEndPoint("/me/playlists"));
        return api;
    }
    
    
    
    
}
