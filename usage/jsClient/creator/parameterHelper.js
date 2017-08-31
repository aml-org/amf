exports.parameterHelper = {
    populateIds: function (ids) {
        ids.withName("displayName")
            .withSchema("String")
            .withDescription("A comma-separated list of IDs")
            .withRequired(true);
    },

    populateMaker: function (ids) {
        ids.withName("Market")
            .withSchema("String")
            .withDescription(" The market (an ISO 3166-1 alpha-2 country code)")
            .withRequired(false);
    },

    populateIdAlbum: function (id) {
        id.withName("Spotify Album ID")
            .withSchema("string")
            .withDescription("The Spotify ID for the albums");
    }
}