var amf = require('../../../amf-js/target/artifact/amf-module.js')

var parser = {
    parse: function (from, mode, content, response) {
        if (mode === 'text') {
            this.parseString(from, content, response)
        } else {
            this.parseFile(from, content, response)
        }
    },


    /** Client example usage of how to invoke amf to parse a api ile*/
    parseFile: function (from, url, response) {
        var parser = this.getParser(from)
        parser.parseFile(url, this.getHanlder(response))
    },

    /** Client example usage of how to invoke amf to parse a api string*/
    parseString: function (from, api, response) {
        var client = this.getParser(from);
        console.log('About to parse: ' + api)
        client.parseString(api, this.getHanlder(response))
    },

    /**Example of how to implement a base unit handler to do some logic with the base unit parsed*/
    getHanlder: function (response) {
        return {
            success: function (doc) {
                console.log('parser has respond with: ' + doc)
                response.writeHead(200, {'Content-Type': 'text/html'})
                response.write("Api parsed. Name: " + doc.encodes.name)

                response.end()
            },
            error: function (exception) {
                console.log("Error", exception)
                response.writeHead(200, {'Content-Type': 'text/html'})
                response.write("Error:" + exception.toString())
                response.end()
            }
        }
    },

    getParser: function (from) {

        if (from === "raml") return new amf.RamlParser()
        if (from === "oas") return new amf.OasParser()
        return new amf.AmfParser()
    }
}

exports.parser = parser;