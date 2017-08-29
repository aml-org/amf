var amf = require('../../../amf-js/target/artifact/amf-module.js')



const stringify = (data) => {
	if (!data) return '';
	if (typeof data === 'string') return data;
	const result = JSON.stringify(data, null, 2);
	return result === '{}' ? '' : result;
};

const resolve = (error, result) => {
	self.postMessage({
		result: stringify(result),
		error: stringify(error),
		message: error ? error.message : ''
	});
};

function getParser(from){
    var parser;
    if(from == 'raml'){
        parser = new amf.RamlParser()
    }else if(from =='oas'){
        parser = new amf.OasParser()
    }else {
        parser
    }
    return parser
}
function getGenerator(to){
    var generator;
    if(to == 'raml'){
        generator = new amf.RamlGenerator()
    }else if(to =='oas'){
        generator = new amf.OasGenerator()
    }else{
        generator = new amf.AmfGenerator()
    }
    return generator
}

self.addEventListener('message', (e) => {
	console.log("amf: "+amf)
	console.log("e: "+e)
	const message = e.data;
	console.log("message: "+ message)
	console.log("rawdata: "+ message.rawData);
	const from = message.fromLanguage.className
	const to = message.toLanguage.className
	var parser = getParser(from)

	var generator=getGenerator(to)

	parser.parseString(message.rawData,{
        success: function(doc){
            console.log("result: "+doc);
            generator.generateString(doc, {
                success: function (doc) {
                    console.log("result: " + doc);
                    resolve(null, doc);
                },
                error: function (exception) {
                    resolve(exception, exception.toString());
                }
            })
        },
        error: function(exception){
            resolve(exception,exception.toString());
        }

    } );
}, false);
