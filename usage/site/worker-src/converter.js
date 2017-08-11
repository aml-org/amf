var amf = require('../../amf-js/target/artifact/amf-module.js')



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
	const message = e.data;

	const client = new amf.JsClient();
	console.log("client: "+ client);
	client.convert(message.rawData,message.fromLanguage.className,message.toLanguage.className,
		{
			success: function(doc){
				resolve(null,doc);
			},
			error: function(exception){
				resolve(exception,exception.toString());
			}

		}
	);
}, false);
