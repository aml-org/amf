var express = require('express');
var fs = require('fs');
var path = require('path');
var amf = require('../../amf-js/target/scala-2.12/amf-fastopt.js')
// respond with "hello world" when a GET request is made to the homepage


var http = require('http');

http.createServer(function (request, response) {
    console.log("Method: "+ request.method + "request url= "+request.url)
    if (request.method==="POST" && request.url.startsWith("/parse")) {
        response.writeHead(200, {'Content-Type': 'text/html'});

        var jsonString = '';
        request.on('data', function (data) {
            jsonString += data;
        });
        request.on('end', function () {
            console.log("Json value: "+ jsonString)

            var client =new amf.JsClient();
            client.webApiClass(jsonString,{
                success: function(doc){
                    console.log("Respondio")
                    console.log("Doc response: "+doc)
                    response.writeHead(200, {'Content-Type': 'text/html'});
                    response.write(JSON.stringify(doc))

                  console.log("NAME: " + doc.name)
                  console.log("DESCR: " + doc.description)
                  console.log("PROVIDER: " + doc.provider)
                  console.log("PROVIDER URL: " + doc.provider.url)
                  console.log("PROVIDER SCHEME: " + doc.scheme)

                  var builder = doc.toBuilder

                  builder.withName("Some other name")

                  var newWebApi = builder.build
                  console.log("NEW NAME: " + newWebApi.name)
                  console.log("OLD DESCR: " + newWebApi.description)

                    response.end()
                },
                error: function(exception){
                    console.log("Error",exception)
                    response.writeHead(200, {'Content-Type': 'text/html'});
                    response.write("Error:" +exception.toString())
                    response.end()
                }

            });
        });
    } else {
        response.writeHead(200, {'Content-Type': 'text/html'});
        fs.createReadStream('sample.html').pipe(response);
    }
}).listen(3000)

