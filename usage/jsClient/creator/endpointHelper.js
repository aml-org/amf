var OperationHelper = require('./operationHelper.js').operationHelper;
var ParameterHelper = require('./parameterHelper.js').parameterHelper;

exports.enpointHelper = {
    populateAlbums: function (albums) {
        albums.withName("several-albums");
        OperationHelper.populateSearchAlbums(albums.withOperation("get"));

    },

    populateAlbumId: function (album) {
        album.withName("albums");
        ParameterHelper.populateIdAlbum(album.withParameter("id"));

        OperationHelper.populateGetAlbum(album.withOperation("get"));

    },

    populateMe: function (me) {
        me.withName("current-user");
        OperationHelper.populateGetUser(me.withOperation("get"));
    },

    populatePaylists: function (playlists) {
        playlists.withName("current-user-playlists");
        OperationHelper.populateListPlaylists(playlists.withOperation("get"));
    }
};


