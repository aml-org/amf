exports.payloadHelper = {

    populate200Playlists: function (payload) {
        payload.withMediaType("application/json")
            .withSchema("string");
    }
}