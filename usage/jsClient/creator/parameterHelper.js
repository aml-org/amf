exports.parameterHelper = {
    populateIds: function (ids) {
        ids.withName("displayName")
            .withDescription("A comma-separated list of IDs")
            .withObjectSchema("String")
    },

    populateMaker: function (ids) {
        ids.withName("Market")
            .withDescription(" The market (an ISO 3166-1 alpha-2 country code)")
            .withObjectSchema("String")
    },

    populateIdAlbum: function (id) {
        id.withName("Spotify Album ID")
            .withDescription("The Spotify ID for the albums")
            .withObjectSchema("string")
    }
}