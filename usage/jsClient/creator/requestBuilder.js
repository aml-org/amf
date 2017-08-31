var amf = require('../../../amf-js/target/artifact/amf-module.js');
var ParameterHelper = require('./parameterHelper.js').parameterHelper;

exports.requestBuilder = {

    albums: function () {
        var request = new amf.Request();
        ParameterHelper.populateIds(request.withQueryParameter("ids"))
        ParameterHelper.populateMaker(request.withQueryParameter("market"))

        return request;
    },

    album: function () {
        var request = new amf.Request()
        ParameterHelper.populateMaker(request.withQueryParameter("market"))

        return request;
    }

}