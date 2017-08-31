var express = require('express');
var fs = require('fs');
var path = require('path');
var amf = require('../../amf-js/target/artifact/amf-module.js')

var parser = require('./parser/parserClient.js').parser;
var generator = require('./generator/generatorClient.js').generator
var creator = require('./creator/documentCreator.js').creator


var http = require('http');

/** http server and other stuff for support the example. Not important to AMF*/
http.createServer(function (request, response) {
    console.log("Method: "+ request.method + "request url= "+request.url)

    if ( request.url.startsWith("/parse") || request.url.startsWith("/generate")) {
        handlerAmfRequest(request,response)

    }else if ( request.url.startsWith("/new") ) {

        createNew(response)

    }else if( request.url.startsWith('/styles')){
            response.writeHead(200, {'Content-Type': 'text/css'});
            fs.createReadStream('styles/style.css').pipe(response);
    } else{
        response.writeHead(200, {'Content-Type': 'text/html'});
        fs.createReadStream('sample.html').pipe(response);
    }
}).listen(3000);


/** Handler requests to amf, parse and generation*/
function handlerAmfRequest(request,response){
    var jsonString = '';
    request.on('data', function (data) {jsonString += data;});

    if(request.url.startsWith("/parse")){

        request.on('end', function () {
            var plain= JSON.parse(jsonString);
            /** For usage example of the parsers see file parser/parserClient.js.
             * There is the how to.
             */
            parser.parse(plain.from,plain.mode,plain.content, response);
        });
    }

    if(request.url.startsWith("/generate")){

        request.on('end', function () {
            var plain= JSON.parse(jsonString);

            /** For usage example of the generators see file generator/generatorClient.js.
            * There is the how to.
            */
            generator.generate(plain.to,plain.mode, plain.path, creator.spotifyApiDocument(),response);
        });
    }

}

/** Creation of new api from scratch*/
function createNew(response){
    var document =  creator.spotifyApiDocument();
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.write(document.encodes)

    response.end()
}

/**Example hoy to mutate and existing api*/
function mutateApi() {
    var document = creator.spotifyApiDocument();

    document.encodes()
        .withDocumentation(new amf.CreativeWork()
            .withUrl("http://example.com/baking.raml")
            .withDescription("ACME Banking HTTP API "));

    document.encodes()
        .withEndPoint("/customer/accounts").withName("Customer Accounts");

    document.encodes()
        .endPoints()[0]
        .withName("New Customer Resource")
        .withOperation("get")
        .withDescription("Gets a customer");
}