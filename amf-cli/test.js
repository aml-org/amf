var amf = require("./amf.js");

amf.plugins.document.WebApi.register();
amf.plugins.document.Vocabularies.register();
amf.plugins.features.AMFValidation.register();

amf.Core.init().then(function () {

    console.log("Registered!");

});
