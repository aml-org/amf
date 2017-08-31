var amf = require('../../../amf-js/target/artifact/amf-module.js')

var generator = {

    generate: function (to, mode, file, content, response) {
        if (mode === 'text') {
            this.generateString(to, content, response)
        } else {
            this.generateFile(to, content, response)
        }
    },

    /** Client example usage of how to invoke amf to generator to dump the api to a file*/
    generateFile: function (to, url, response) {
        var generator = this.getGenerator(to);
        generator.generateFile(url, this.getHanlder(response))
    },

    /** Client example usage of how to invoke amf to generator to dump the api to a string*/
    generateString: function (to, api, response) {
        var generator = this.getGenerator(to);
        generator.generateString(api, this.getHanlder(response))
    },

    /**Example of how to implement a base unit handler to do some logic with the dumped api*/

    getHanlder: function (response) {
        return {
            success: function (doc) {
                response.writeHead(200, {'Content-Type': 'text/html'})
                response.write(doc)

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

    getGenerator: function (to) {

        if (to === "raml") return new amf.RamlGenerator()
        if (to === "oas") return new amf.OasGenerator()
        return new amf.AmfGenerator();
    }
}

exports.generator = generator;