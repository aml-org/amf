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

self.addEventListener('message', (e) => {
	console.log("amf: "+amf)
	console.log("e: "+e)
	const message = e.data;
	console.log("message: "+ message)
	const client = new amf.JsClient();
	console.log("client: "+ client);
	console.log("rawdata: "+ message.rawData);
	const hint=amf.HintMatcherHelper.matchSourceHint(message.fromLanguage.className)
	const generator = new amf.JsGenerator();

	const toVendor =  amf.HintMatcherHelper.matchToVendor(message.toLanguage.className)

	client.convert(message.rawData,
		hint,
		toVendor,
		{
			success: function(doc){
				console.log("result: "+doc);
				resolve(null,doc);
			},
			error: function(exception){
				resolve(exception,exception.toString());
			}

		}
	);
}, false);
