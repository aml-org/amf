package amf.example.creator;

import amf.model.EndPoint;

/**
 * Helper class to populate a given web api endpoint and subclasses with example data.
 */
class EndPointHelper {
    
    static void populateAlbums(EndPoint albums){
        albums.withName("several-albums");
        OperationHelper.populateSearchAlbums(albums.withOperation("get"));
        
    }
    
    static void populateAlbumId(EndPoint album){
        album.withName("albums");
        ParameterHelper.populateIdAlbum(album.withParameter("id"));
        
        OperationHelper.populateGetAlbum(album.withOperation("get"));
        
    }
    
    static void populateMe(EndPoint me) {
        me.withName("current-user");
        OperationHelper.populateGetUser(me.withOperation("get"));
    }
    
    static void populatePaylists(EndPoint playlists) {
        playlists.withName("current-user-playlists");
        OperationHelper.populateListPlaylists(playlists.withOperation("get"));
    }
}
