var PayloadHelper = require('./payloadHelper.js').payloadHelper;

exports.responseHelper = {

    populate200Playlists: function (response) {
        PayloadHelper.populate200Playlists(response.withPayload())
    }
}