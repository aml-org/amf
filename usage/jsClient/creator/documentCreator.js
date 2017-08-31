var amf = require('../../../amf-js/target/artifact/amf-module.js');
var EndPointHelper = require('./endpointHelper.js').enpointHelper;

/**
 * Client class for create a document with a web api from scratch. Use this code as example for build apis programmatically
 */
var creator = {

    spotifyApiDocument: function () {
        return new amf.Document(this.spotifyApi());
    },

    spotifyApi: function () {
        var api = new amf.WebApi();
        api.withName("Spotify Web API")
            .withVersion("v1")
            .withBasePath("{version}")
            .withContentType(["application/json"])
            .withAccepts(["application/json"]);

        EndPointHelper.populateAlbums(api.withEndPoint("/albums"));
        EndPointHelper.populateAlbumId(api.withEndPoint("/albums/{id}"));
        EndPointHelper.populateMe(api.withEndPoint("/me"));
        EndPointHelper.populatePaylists(api.withEndPoint("/me/playlists"));
        return api;
    }
}

exports.creator = creator;