var ResponseHelper = require('./responseHelper.js').responseHelper;
var RequestBuilder = require('./requestBuilder.js').requestBuilder;


exports.operationHelper = {

    populateSearchAlbums: function (searchAlbums) {
        searchAlbums
            .withDescription("[Get Several Albums](https://developer.spotify.com/web-api/get-several-albums/)")
        searchAlbums.withRequest(RequestBuilder.albums())
    },

    populateGetAlbum: function (getAlbum) {

        getAlbum.withDescription(" [Get an Album](https://developer.spotify.com/web-api/get-albums/)")
            .withRequest(RequestBuilder.album())
    },

    populateGetUser: function (getUser) {
        getUser.withDescription("[Get Current User's Profile](https://developer.spotify.com/web-api/get-current-users-profile/)")
    },

    populateListPlaylists: function (listPlaylists) {
        listPlaylists.withDescription("[Get a List of Current User's Playlists](https://developer.spotify.com/web-api/get-a-list-of-current-users-playlists/)")
        ResponseHelper.populate200Playlists(listPlaylists.withResponse("200"))
    }
};